# DYNAMIC DATA STRATEGY — FruitShop Gatling Performance Tests

> Dựa trên phân tích source code thực tế tại `src/main/java/server/FruitShop/`

---

## 1. Tổng quan — Dynamic vs Static

| Loại | Chiến lược | Gatling API |
|------|-----------|-------------|
| Static | CSV Feeder | `.feed(csv("..."))` |
| Dynamic | Capture từ API response | `.check(jsonPath(...).saveAs(...))` |
| DB-seeded | Đọc từ DB trước khi test | Pre-loaded, dùng Feeder |

---

## 2. Bảng Dynamic Data — Source Code Verified

| Data | Tạo bởi | Endpoint | JSONPath capture | Gatling variable | Dùng bởi |
|------|---------|----------|-----------------|-----------------|---------|
| `accountId` | Login / Register | `POST /api/account` hoặc `POST /api/account/login` | `$.accountId` | `accountId` | Cart, Order, Chat, Rating, Refund |
| `cartId` | Create/Get Cart | `POST /api/cart/account/{accountId}` hoặc `GET /api/cart/account/{accountId}` | `$.cartId` | `cartId` | Cart item ops, Clear cart |
| `cartItemId` | Add Cart Item | `POST /api/cart/account/{accountId}/items` | `$.cartItemId` | `cartItemId` | PUT/DELETE `/api/cart/items/{cartItemId}` |
| `shippingId` | Create Shipping (template) | `POST /api/shipping` | `$.shippingId` | `shippingId` | `POST /api/order` field `shippingId` |
| `orderId` | Create Order | `POST /api/order` | `$.orderId` | `orderId` | MoMo payment, Order history, Refund |
| `paymentId` | Create Payment | `POST /api/payment` | `$.paymentId` | `paymentId` | `POST /api/order` field `paymentId` (optional, nếu pre-create) |
| `sessionId` | Create Chat Session | `POST /api/chat/sessions` | `$.sessionId` | `sessionId` | Chat message, Get messages, Close session |
| `orderItemId` | From Order response | `POST /api/order` | `$.orderItems[0].orderDetailId` | `orderItemId` | Rating (`orderItemId`), Refund (`orderItemId`) |
| `productId` | From product list | `GET /api/product` | `$.content[0].productId` | `productId` | Add to cart, Filter, Rating |

---

## 3. Correlation Flow Chi Tiết (Source-Verified)

### 3.1 accountId — Từ Login Response
```
POST /api/account/login
  Body: { "accountPhone": "#{accountPhone}", "password": "#{password}" }
        ↓
  Response: AccountResponse (không wrap trong key khác — trả thẳng object)
        ↓
  JSONPath: $.accountId
        ↓
  .check(jsonPath("$.accountId").saveAs("accountId"))
        ↓
  Session variable: accountId
        ↓
  Dùng: GET /api/cart/account/#{accountId}
         POST /api/cart/account/#{accountId}/items
         GET /api/order/account/#{accountId}
         POST /api/chat/sessions  body: { "accountId": "#{accountId}" }
```

> **⚠️ QUAN TRỌNG**: AccountController.login() trả `AccountResponse` trực tiếp, KHÔNG wrap trong `{ "data": ... }` hay `{ "token": ... }`. Không có JWT — auth là stateless không token.

---

### 3.2 cartId — Từ Get hoặc Create Cart
```
GET /api/cart/account/#{accountId}         (check nếu đã có)
  hoặc
POST /api/cart/account/#{accountId}        (nếu chưa có)
        ↓
  Response: CartResponse { cartId, accountId, items, status, ... }
        ↓
  JSONPath: $.cartId
        ↓
  .check(jsonPath("$.cartId").saveAs("cartId"))
        ↓
  Dùng: POST /api/cart/account/#{accountId}/items
         GET /api/cart/account/#{accountId}/items
         DELETE /api/cart/account/#{accountId}/clear
```

> **⚠️ NOTE**: `CartServiceImpl.createCart()` kiểm tra nếu cart đã tồn tại thì trả về existing cart. Tức là `POST /api/cart/account/{id}` là idempotent — an toàn gọi nhiều lần.

---

### 3.3 cartItemId — Từ Add Item Response
```
POST /api/cart/account/#{accountId}/items
  Body: { "productId": "#{productId}", "quantity": 2 }
        ↓
  Response: CartItemResponse { cartItemId, productId, quantity, ... }
        ↓
  JSONPath: $.cartItemId
        ↓
  .check(jsonPath("$.cartItemId").saveAs("cartItemId"))
        ↓
  Dùng: PUT /api/cart/items/#{cartItemId}
         DELETE /api/cart/items/#{cartItemId}
```

---

### 3.4 shippingId — Từ Create Shipping Template
```
POST /api/shipping
  Body: {
    "accountId": "#{accountId}",
    "receiverName": "#{receiverName}",
    "receiverPhone": "#{receiverPhone}",
    "receiverAddress": "#{receiverAddress}",
    "city": "#{city}",
    "shipperName": "#{shipperName}",
    "shippingFee": #{shippingFee}
  }
        ↓
  Response: ShippingResponse { shippingId, accountId, receiverName, ... }
        ↓
  JSONPath: $.shippingId
        ↓
  .check(jsonPath("$.shippingId").saveAs("shippingId"))
        ↓
  Dùng: POST /api/order  body.shippingId = "#{shippingId}"
```

> **⚠️ NOTE từ OrderServiceImpl**: Order không dùng `shippingId` trực tiếp như FK. Thay vào đó, `OrderServiceImpl.createOrder()` **copy** thông tin từ shipping template vào một shipping record mới. Nên shipping template (với `order=NULL`) có thể tái sử dụng nhiều lần.

---

### 3.5 orderId — Từ Create Order Response
```
POST /api/order
  Body: {
    "accountId": "#{accountId}",
    "shippingId": "#{shippingId}",
    "paymentMethod": 0,
    "totalPrice": #{totalPrice},
    "items": [
      { "productId": "#{productId}", "unitPrice": #{unitPrice}, "quantity": 2, "totalPrice": #{totalPrice} }
    ]
  }
        ↓
  Response: OrderResponse { orderId, accountId, status, totalAmount, orderItems, ... }
        ↓
  JSONPath: $.orderId
        ↓
  .check(jsonPath("$.orderId").saveAs("orderId"))
        ↓
  Dùng: POST /api/momo/create-payment  body: { "orderId": "#{orderId}" }
         GET /api/momo/check-status/#{orderId}
         POST /api/refund  body.orderId = "#{orderId}"
```

---

### 3.6 orderItemId — Từ Order Response Items
```
POST /api/order  (sau khi tạo xong)
        ↓
  Response: OrderResponse { ..., orderItems: [ { orderDetailId, productId, ... } ] }
        ↓
  JSONPath: $.orderItems[0].orderDetailId
        ↓
  .check(jsonPath("$.orderItems[0].orderDetailId").saveAs("orderItemId"))
        ↓
  Dùng: POST /api/rating  body.orderItemId = "#{orderItemId}"
         POST /api/refund  body.orderItemId = "#{orderItemId}"
```

> **⚠️ Entity field**: `OrderItem.orderDetailId` (không phải `orderItemId`). Kiểm tra `OrderItemResponse` DTO để xác nhận field name trong JSON response.

---

### 3.7 sessionId — Từ Create Chat Session
```
POST /api/chat/sessions
  Body: { "accountId": "#{accountId}", "title": "Tư vấn hoa quả" }
        ↓
  Response: ChatSessionResponse { sessionId, accountId, title, status, ... }
        ↓
  JSONPath: $.sessionId
        ↓
  .check(jsonPath("$.sessionId").saveAs("sessionId"))
        ↓
  Dùng: POST /api/chat/messages  body.sessionId = "#{sessionId}"
         GET /api/chat/messages/#{sessionId}
         PATCH /api/chat/sessions/#{sessionId}/read
```

---

### 3.8 productId — Từ Product Listing (Dynamic for Guest Flow)
```
GET /api/product?page=0&size=10
        ↓
  Response: Page<ProductResponse> { content: [ { productId, ... } ] }
        ↓
  JSONPath: $.content[0].productId
        ↓
  .check(jsonPath("$.content[0].productId").saveAs("productId"))
        ↓
  Dùng: GET /api/product/#{productId}
         GET /api/rating/product/#{productId}
         POST /api/cart/account/#{accountId}/items  body.productId = "#{productId}"
```

---

## 4. Workflow → Data Mapping

### Workflow 1 — Guest Browsing (No auth)
| Data | Type | Source |
|------|------|--------|
| `keywords` | Static feeder | `search_keywords.csv` |
| `minPrice`, `maxPrice` | Static feeder | `price_ranges.csv` |
| `productId` | Dynamic | `$.content[0].productId` từ GET /api/product |

### Workflow 2 — Registration & Login
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password`, `accountName` | Static feeder | `accounts.csv` |
| `accountId` | Dynamic | `$.accountId` từ POST /api/account hoặc login |

### Workflow 3 — Cart Management
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password` | Static feeder | `accounts.csv` |
| `accountId` | Dynamic | từ login |
| `cartId` | Dynamic | `$.cartId` từ GET/POST /api/cart/account/{id} |
| `cartItemId` | Dynamic | `$.cartItemId` từ POST /api/cart/account/{id}/items |
| `productId` | Static (seed) | `lt-prod-001` đến `lt-prod-050` (random pick) |

### Workflow 4 — COD Checkout
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password` | Static feeder | `accounts.csv` |
| `accountId` | Dynamic | từ login |
| `shippingId` | Dynamic | từ POST /api/shipping hoặc seed `lt-ship-xxx` |
| `orderId` | Dynamic | `$.orderId` từ POST /api/order |
| `orderItemId` | Dynamic | `$.orderItems[0].orderDetailId` |
| `productId` | Static (seed) | `lt-prod-001` đến `lt-prod-010` |

### Workflow 5 — MoMo Payment
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password` | Static feeder | `accounts.csv` |
| `accountId` | Dynamic | từ login |
| `shippingId` | Dynamic/Seed | từ POST /api/shipping |
| `orderId` | Dynamic | `$.orderId` từ POST /api/order |

> ⚠️ MoMo IPN test cần signature hợp lệ — xem mục 5.

### Workflow 6 — AI Chatbot
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password` | Static feeder | `accounts.csv` |
| `accountId` | Dynamic | từ login (hoặc null nếu guest) |
| `sessionId` | Dynamic | `$.sessionId` từ POST /api/chat/sessions |
| `content`, `intent` | Static feeder | `chat_messages.csv` |

### Workflow 7 — Rating & Refund
| Data | Type | Source |
|------|------|--------|
| `accountPhone`, `password` | Static feeder | `accounts.csv` (accounts 081-100) |
| `accountId` | Dynamic | từ login |
| `orderId` | **DB Seeded** | `lt-ord-081` đến `lt-ord-100` |
| `orderItemId` | **DB Seeded** | `lt-oi-081-1` đến `lt-oi-100-2` |
| `productId` | **DB Seeded** | linked với orderItems |

> Lý do seed: Rating cần `orderItemId` hợp lệ thuộc về account, và order phải là completed. Không thể tạo dynamically trong 1 test run vì cần admin approve order trước.

---

## 5. Ghi chú đặc biệt

### MoMo IPN Signature
MoMo IPN handler (`POST /api/momo/ipn-handler`) **verify signature** bằng HMAC SHA256. Kết quả:
- Nếu signature sai → HTTP 401 (Unauthorized)
- Test performance sẽ return 401 nếu không có signature đúng

**Hai chiến lược**:
1. **Mock/bypass**: Tạm thời comment `momoService.verifySignature()` trong test environment
2. **Generate signature đúng**: Implement Gatling utility tính HMAC SHA256 trước khi gửi

**NEED CONFIRMATION**: Bạn có muốn mock IPN trong load test hay test end-to-end với MoMo sandbox?

### Không có JWT / Session Token
- `AccountController.login()` trả về `AccountResponse` với `accountId`, **không có JWT token**
- Spring Security bị comment (`// @PreAuthorize(...)` ở tất cả endpoints)
- Không cần Bearer token header → đơn giản hóa Gatling setup

### Chat Guest Mode
- `CreateSessionRequest.accountId` là nullable → có thể tạo session không cần login
- Workflow 6 có thể test cả guest và authenticated chat

---

## 6. Seeded Data Cho Workflow 7

Các account 081-100 đã được seed với:
```
Account lt-acc-081 → Order lt-ord-081 (status=4 COMPLETED) → OrderItem lt-oi-081-1/2
                                       Payment lt-pay-081 (status=1 PAID)
```

Để dùng trong Gatling, tạo CSV riêng sau khi seed:
```csv
accountPhone,orderId,orderItemId,productId,paymentId
0900000081,lt-ord-081,lt-oi-081-1,lt-prod-001,lt-pay-081
0900000082,lt-ord-082,lt-oi-082-1,lt-prod-003,lt-pay-082
...
```

Sau đó Gatling dùng `.feed(csv("seeded_orders.csv"))` để load.
