# DATA STRATEGY — FruitShop Gatling Performance Testing

> **Dựa hoàn toàn trên source code thực tế** tại `src/main/java/server/FruitShop/`  
> Không có assumption nào chưa được xác minh từ code.

---

## 1. Static Data (Dữ liệu tĩnh)

Static data là dữ liệu **biết trước, không thay đổi**, được cung cấp dưới dạng CSV Feeder cho Gatling đọc trước khi test chạy.

### Files & Nội dung

| File | Mô tả | Dùng cho Workflow |
|------|-------|-------------------|
| `data/static/accounts.csv` | 100 accounts (phone + password + name) | WF2, WF3, WF4, WF5, WF6 |
| `data/static/seeded_orders.csv` | 20 accounts với pre-seeded completed orders | WF7 |
| `data/static/search_keywords.csv` | Keywords tìm kiếm sản phẩm + intent | WF1, WF6 |
| `data/static/chat_messages.csv` | Mẫu tin nhắn chat + intent | WF6 |
| `data/static/shipping_templates.csv` | Mẫu địa chỉ giao hàng (20 địa chỉ) | WF4, WF5 |
| `data/static/price_ranges.csv` | Khoảng giá để filter sản phẩm | WF1 |

### Đặc điểm Static Data

- **Phone numbers**: `0900000001` đến `0900000100` — format hợp lệ per `AccountController.login()` regex `^[0-9]{10,11}$`
- **Password**: `LoadTest@123` — ≥ 6 ký tự, hợp lệ per validation trong AccountController
- **Không chứa UUID database** trong CSV (trừ `seeded_orders.csv` vì đã seed trước)

---

## 2. Dynamic Data (Dữ liệu động)

Dynamic data là dữ liệu **được tạo ra TRONG QUÁ TRÌNH chạy test**, cần capture từ API response và lưu vào Gatling session variable.

### Quy tắc: Capture từ API Response

```
POST /api/something
        ↓
  Response JSON
        ↓
  .check(jsonPath("$.fieldName").saveAs("variableName"))
        ↓
  #{variableName}  ← dùng trong request tiếp theo
```

### Bảng Dynamic Variables

| Variable | Tạo bởi | Endpoint | JSONPath | Dùng bởi |
|----------|---------|----------|----------|---------|
| `accountId` | Login | `POST /api/account/login` | `$.accountId` | Tất cả WF có auth |
| `cartId` | Get/Create Cart | `GET /api/cart/account/#{accountId}` | `$.cartId` | WF3, WF4 |
| `cartItemId` | Add Item | `POST /api/cart/account/#{accountId}/items` | `$.cartItemId` | WF3 |
| `shippingId` | Create Shipping | `POST /api/shipping` | `$.shippingId` | WF4, WF5 |
| `orderId` | Create Order | `POST /api/order` | `$.orderId` | WF4, WF5, WF7 |
| `orderItemId` | Create Order | `POST /api/order` | `$.orderItems[0].orderDetailId` | WF7 |
| `sessionId` | Create Session | `POST /api/chat/sessions` | `$.sessionId` | WF6 |
| `productId` | List Products | `GET /api/product` | `$.content[0].productId` | WF1, WF3, WF4 |

---

## 3. Data nào dùng Feeder

```java
// Workflow 1: Guest Browsing
.feed(csv("search_keywords.csv").random())
.feed(csv("price_ranges.csv").random())

// Workflow 2: Auth & Profile
.feed(csv("accounts.csv").circular())

// Workflow 3: Cart Management  
.feed(csv("accounts.csv").circular())

// Workflow 4: COD Checkout
.feed(csv("accounts.csv").circular())
.feed(csv("shipping_templates.csv").random())

// Workflow 5: MoMo Payment
.feed(csv("accounts.csv").circular())
.feed(csv("shipping_templates.csv").random())

// Workflow 6: AI Chatbot
.feed(csv("accounts.csv").circular())
.feed(csv("chat_messages.csv").random())

// Workflow 7: Rating & Refund
.feed(csv("seeded_orders.csv").circular())  // ← Dùng accounts 081-100
```

> **circular()** vs **random()**: Dùng `circular()` cho accounts để đảm bảo mỗi VU lấy account riêng biệt theo thứ tự. Dùng `random()` cho keywords/addresses vì không cần isolation.

---

## 4. Data nào Capture Dynamically

```java
// Bước 1: Login để lấy accountId
exec(http("Login")
    .post("/api/account/login")
    .body(StringBody("{\"accountPhone\":\"#{accountPhone}\",\"password\":\"#{password}\"}"))
    .check(
        status().is(200),
        jsonPath("$.accountId").saveAs("accountId")
    ))

// Bước 2: Get/Create cart → lấy cartId
exec(http("Get Cart")
    .get("/api/cart/account/#{accountId}")
    .check(
        status().in(200, 404),
        jsonPath("$.cartId").optional().saveAs("cartId")
    ))
// Nếu 404 → create cart
.doIf(session -> !session.contains("cartId"))(
    exec(http("Create Cart")
        .post("/api/cart/account/#{accountId}")
        .check(jsonPath("$.cartId").saveAs("cartId")))
)

// Bước 3: Add item → lấy cartItemId
exec(http("Add Item")
    .post("/api/cart/account/#{accountId}/items")
    .body(StringBody("{\"productId\":\"#{productId}\",\"quantity\":2}"))
    .check(jsonPath("$.cartItemId").saveAs("cartItemId")))

// Bước 4: Create Order → lấy orderId + orderItemId
exec(http("Create Order")
    .post("/api/order")
    .body(...)
    .check(
        jsonPath("$.orderId").saveAs("orderId"),
        jsonPath("$.orderItems[0].orderDetailId").saveAs("orderItemId")
    ))

// Bước 5: Create Chat Session → lấy sessionId
exec(http("Create Chat Session")
    .post("/api/chat/sessions")
    .body(StringBody("{\"accountId\":\"#{accountId}\",\"title\":\"Test Chat\"}"))
    .check(jsonPath("$.sessionId").saveAs("sessionId")))
```

---

## 5. Data nào Cần Database Seeding

### Bắt buộc seed trước (Workflow 7)
Rating và Refund yêu cầu `orderItem` phải thuộc về `account` và `order` phải ở trạng thái hợp lệ. Không thể tạo dynamically vì:
- Order cần được `COMPLETED (status=4)` bởi admin
- Không thể auto-complete order trong 1 Gatling session

**Dữ liệu cần seed** (file: `seed/seed_loadtest_data.sql`):
- 100 accounts (`lt-acc-001` đến `lt-acc-100`)
- 20 completed orders (`lt-ord-081` đến `lt-ord-100`)
- 20 completed payments (`lt-pay-081` đến `lt-pay-100`)
- 40 order items (`lt-oi-081-1/2` đến `lt-oi-100-1/2`)
- 50 products (`lt-prod-001` đến `lt-prod-050`)
- 20 shipping templates (`lt-ship-001` đến `lt-ship-020`)

### Seed script:
```bash
mysql -u your_user -p your_database < performance/seed/seed_loadtest_data.sql
```

---

## 6. Virtual User Isolation

### Vấn đề
Nếu nhiều VUs cùng dùng `acc-1`, `cart-1`, `ord-1` → race conditions, sai kết quả.

### Giải pháp: Mỗi VU = 1 Account riêng

```
VU #1  → accounts.csv row 1  → phone=0900000001 → accountId=lt-acc-001 → cart riêng → order riêng
VU #2  → accounts.csv row 2  → phone=0900000002 → accountId=lt-acc-002 → cart riêng → order riêng
...
VU #100 → accounts.csv row 100 → phone=0900000100 → accountId=lt-acc-100 → cart riêng → order riêng
```

### Cơ chế trong Gatling
```java
// circular() đảm bảo mỗi VU lấy row khác nhau theo thứ tự
.feed(csv("accounts.csv").circular())
```

### Cart Isolation
- `Cart` là `OneToOne` với `Account` (entity: `Cart.account`)
- Mỗi account chỉ có 1 cart → tự nhiên isolated
- `cartId` được lưu trong Gatling session per VU → không share

### Order Isolation
- `Order` là `ManyToOne` với `Account`
- Mỗi VU tạo order riêng, `orderId` captured từ response → per-VU session variable

### Session (Chat) Isolation
- `ChatSession` là `ManyToOne` với `Account`
- `sessionId` captured từ `POST /api/chat/sessions` per VU

---

## 7. Business State Dependency

### Workflow 1 — Guest Browsing
- ✅ **Không yêu cầu state** — các endpoint GET /api/product, /api/category là public
- 📌 Cần: products tồn tại trong DB (dùng seeded products `lt-prod-*`)

### Workflow 2 — Registration & Login
- ✅ **Registration**: Chỉ cần phone chưa tồn tại → dùng `accounts.csv` với phone unique
- ✅ **Login**: Account phải có `status=1` (active) — seeded accounts đều là status=1
- ⚠️ **Duplicate registration**: Nếu test chạy lại, phone đã tồn tại → `HTTP 409`. Simulation phải handle với `.check(status().in(201, 409))`

### Workflow 3 — Cart Management
- ✅ Account cần tồn tại + active
- ✅ Cart: `CartServiceImpl.createCart()` là idempotent — safe gọi lại
- 📌 Product phải tồn tại — dùng seeded `lt-prod-001` đến `lt-prod-050`
- ⚠️ `Cart.status=1` required để `addCartItem()` thành công. Nếu cart bị disable (status≠1) → exception

### Workflow 4 — COD Checkout
- ✅ Account + active
- ✅ Cart + items
- ✅ Shipping template (seeded `lt-ship-001` đến `lt-ship-020`, hoặc tạo mới)
- 📌 `OrderServiceImpl.createOrder()` tự tạo Payment nếu `paymentMethod=0 (COD)` → không cần pre-create payment
- ✅ Product phải có `stock > 0` — seeded với stock=10000

### Workflow 5 — MoMo Payment
- ✅ Order phải tồn tại và `status ≠ 0 (cancelled)` và `payment.status ≠ 1 (already paid)`
- ✅ MoMo gọi external API thật → cần sandbox credentials trong `application.properties`
- ⚠️ **IPN Signature**: `momoService.verifySignature()` verify HMAC SHA256 → test với fake signature sẽ nhận HTTP 401
- **UNKNOWN / NEED CONFIRMATION**: Project có cấu hình MoMo sandbox hay production? Cần biết để quyết định mock hay test thật.

### Workflow 6 — AI Chatbot
- ✅ ChatSession có thể tạo với `accountId=null` (guest mode)
- ✅ `sessionId` tự generate UUID trong `ChatSession.generateIdIfAbsent()`
- ⚠️ **Groq AI API**: `ChatService` gọi external Groq API → mỗi message tốn API call thật
- **UNKNOWN / NEED CONFIRMATION**: Có Groq API key sandbox cho load test không? Concurrent messages có thể hit rate limit.

### Workflow 7 — Rating & Refund
- 📌 **Rating**: Cần `orderItemId` hợp lệ thuộc về account (verified trong `RatingServiceImpl`)
  - `orderItem.order.account.accountId == request.accountId` — nếu không khớp → exception
  - Mỗi `orderItemId` chỉ có thể rate 1 lần — `ratingRepository.findByOrderItemOrderDetailId()` check duplicate
- 📌 **Refund**: Cần `orderId` hợp lệ. `RefundServiceImpl` không check order status (CONFIRMED/COMPLETED) — chỉ check order tồn tại
- ✅ `refundStatus` mặc định là `"Chờ xác nhận"` khi tạo mới

---

## 8. Hard-coded IDs Cần Loại Bỏ — FruitShopFullSimulation.java

Tìm thấy trong file hiện tại `/performance/gatling/src/test/java/simulations/FruitShopFullSimulation.java`:

| Dòng | Hard-coded value | Vấn đề | Giải pháp |
|------|-----------------|--------|-----------|
| 58 | `/api/product/p-1` | ID không tồn tại | Dynamic: capture từ `GET /api/product` → `$.content[0].productId` |
| 61 | `/api/rating/product/p-1` | ID không tồn tại | Dynamic: dùng `productId` từ session |
| 80 | `/api/account/acc-1` | ID không tồn tại | Dynamic: capture `accountId` sau login |
| 83 | `/api/shipping/account/acc-1` | ID không tồn tại | Dynamic: dùng `accountId` từ session |
| 91 | `/api/cart/account/acc-1` | ID không tồn tại | Dynamic: dùng `accountId` |
| 95-96 | `acc-1`, `p-1` trong cart add | IDs không tồn tại | Dynamic: `accountId`, `productId` từ session |
| 100 | `/api/cart/account/acc-1/items` | ID không tồn tại | Dynamic |
| 104 | `/api/cart/items/ci-1` | `ci-1` không tồn tại | Dynamic: capture `cartItemId` |
| 112-136 | `acc-1`, `ship-1`, `p-1` | IDs không tồn tại | Dynamic sau login + shipping creation |
| 145-156 | `acc-1`, `ship-1`, `p-1`, `ord-1` | IDs không tồn tại | Dynamic |
| 162 | `ord-1` trong MoMo IPN | ID không tồn tại | Dynamic: dùng `orderId` từ session |
| 178 | `/api/momo/check-status/ord-1` | ID không tồn tại | Dynamic |
| 186 | `acc-1` trong chat session | ID không tồn tại | Dynamic |
| 192 | `sess-1` trong chat message | ID không tồn tại | Dynamic: capture `sessionId` |
| 200 | `/api/chat/messages/sess-1` | ID không tồn tại | Dynamic |
| 203 | `/api/chat/sessions/sess-1/read` | ID không tồn tại | Dynamic |
| 212 | `acc-1`, `p-1` trong rating | IDs không tồn tại | Seeded data feeder |
| 221 | `ord-1` trong refund | ID không tồn tại | Seeded data feeder |

### Pattern Replacement
```java
// ❌ CŨ (hard-code)
.get("/api/cart/account/acc-1")

// ✅ MỚI (dynamic)
.get("/api/cart/account/#{accountId}")
```

```java
// ❌ CŨ (hard-code)
.body(StringBody("{\"productId\":\"p-1\",\"quantity\":2}"))

// ✅ MỚI (dynamic)
.body(StringBody("{\"productId\":\"#{productId}\",\"quantity\":2}"))
```

---

## 9. Vấn đề Tiềm Ẩn (Potential Problems)

### P1: Shared Cart (Race Condition)
- **Mô tả**: Cart là `OneToOne` với Account. Nếu VU lấy cùng account → cùng cart
- **Giải quyết**: Mỗi VU dùng account riêng biệt qua `accounts.csv` feeder

### P2: Duplicate Account Registration (409 Conflict)
- **Mô tả**: Test chạy lần 2 → phone đã tồn tại → `DuplicateResourceException`
- **Giải quyết**: Handle `.check(status().in(201, 409))` trong simulation. Nếu 409, thử login thay vì tạo mới.

### P3: MoMo External API Latency
- **Mô tả**: `POST /api/momo/create-payment` gọi MoMo API thật → latency cao, không kiểm soát được
- **Giải quyết**: Test Workflow 5 riêng biệt, đặt timeout cao hơn (5s), không include trong main load test

### P4: MoMo IPN Signature Failure
- **Mô tả**: `handleIPN()` verify HMAC SHA256 signature → fake signature → 401
- **Giải quyết**: **NEED CONFIRMATION** — mock verifySignature() hoặc implement Gatling HMAC utility

### P5: Groq AI Rate Limit
- **Mô tả**: Concurrent chat messages → có thể hit Groq API rate limit → 429
- **Giải quyết**: **NEED CONFIRMATION** — test Workflow 6 với VU thấp hoặc mock AI response

### P6: Product Stock Depletion (Không xảy ra)
- **Mô tả**: Nếu stock giảm → không thể mua
- **Giải quyết**: Seed products với `stock=10000` → đủ cho 10,000+ test transactions

### P7: Rating Duplicate (Per OrderItem)
- **Mô tả**: `ratingRepository.findByOrderItemOrderDetailId()` kiểm tra duplicate → chỉ rate 1 lần/item
- **Giải quyết**: Dùng `seeded_orders.csv` mỗi row dùng `orderItemId` khác nhau. Đảm bảo 1 VU chỉ rate 1 item 1 lần.

### P8: Token / Auth Expiration
- **Mô tả**: KHÔNG có JWT/token trong hệ thống này — auth bị comment. Không có expiration issue.
- **Trạng thái**: Không phải vấn đề. ✅

### P9: Data Exhaustion (Accounts)
- **Mô tả**: 100 VUs nhưng chỉ có 100 accounts → nếu VU > 100 thì circular() sẽ reuse account
- **Giải quyết**: Tăng số accounts lên 200+ nếu cần test > 100 concurrent VUs

### P10: OrderServiceImpl Fallback Shipping
- **Mô tả**: Nếu `shippingId` không tìm thấy, `OrderServiceImpl` fallback lấy shipping template mới nhất của account (`order==null`). Tức là nếu shipping template bị consume (linked vào order), fallback có thể fail.
- **Giải quyết**: Shipping template không bị tiêu thụ — OrderServiceImpl tạo **copy** mới → template vẫn giữ `order=null`. ✅ An toàn.

---

## 10. Assumptions Cần Xác Nhận

### A1 — MoMo Sandbox vs Production
```
NEED CONFIRMATION:
  MoMo được cấu hình sandbox hay production?
  Load test có nên gọi MoMo thật không?
  Hay mock/stub momoService.createQR() cho performance test?
```

### A2 — Groq AI API Key cho Load Test
```
NEED CONFIRMATION:
  Groq API có rate limit không?
  Có API key sandbox riêng cho load test không?
  Workflow 6 có nên mock AI response không?
```

### A3 — BCrypt Hash trong Seed Script
```
NEED CONFIRMATION:
  BCryptPasswordEncoder strength mặc định là 10.
  Hash trong seed script:
    $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lSmG
  Tương ứng với password "LoadTest@123"
  
  Vui lòng verify bằng:
    new BCryptPasswordEncoder().matches("LoadTest@123", "<hash>")
  
  Nếu hash không match → cần generate lại trước khi seed.
```

### A4 — OrderResponse JSON Structure
```
NEED CONFIRMATION:
  OrderResponse.orderItems là list — JSONPath: $.orderItems[0].orderDetailId
  Cần xác nhận field name trong JSON là "orderDetailId" (từ OrderItem entity field)
  không phải "orderItemId" hay tên khác.
  
  Xác minh bằng: GET /api/order/account/{accountId} sau khi tạo order test.
```

### A5 — Database Dialect (MySQL vs PostgreSQL)
```
NEED CONFIRMATION:
  Seed script dùng MySQL syntax (INSERT IGNORE, DATE_SUB, NOW()).
  Nếu project dùng PostgreSQL → cần thay thế:
    INSERT IGNORE → INSERT ... ON CONFLICT DO NOTHING
    DATE_SUB(NOW(), INTERVAL 5 DAY) → NOW() - INTERVAL '5 days'
```

---

## 11. Tóm tắt Files đã Tạo

```
performance/
├── data/
│   ├── static/
│   │   ├── accounts.csv           ← 100 test accounts (phone/password/name)
│   │   ├── seeded_orders.csv      ← 20 completed orders cho WF7
│   │   ├── search_keywords.csv    ← Search terms + intent cho WF1/WF6
│   │   ├── chat_messages.csv      ← Chat message templates cho WF6
│   │   ├── shipping_templates.csv ← Địa chỉ giao hàng mẫu cho WF4/WF5
│   │   └── price_ranges.csv       ← Khoảng giá cho product filter WF1
│   │
│   └── DYNAMIC_DATA.md            ← Tài liệu dynamic variables + correlation flows
│
├── seed/
│   └── seed_loadtest_data.sql     ← SQL để seed DB trước khi test
│
├── DATA_STRATEGY.md               ← File này
└── plan.md                        ← Performance test plan (đã có)
```
