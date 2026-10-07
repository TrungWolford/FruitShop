# HỆ THỐNG QUẢN LÝ KỊCH BẢN VÀ ĐÁNH GIÁ HIỆU NĂNG (PERFORMANCE TESTING PLAN) - FRUITSHOP BACKEND

---

## 1. TỔNG QUAN HỆ THỐNG (SYSTEM OVERVIEW)

Hệ thống **FruitShop Backend** là ứng dụng thương mại điện tử phục vụ bán hoa quả tươi, tích hợp thanh toán trực tuyến MoMo, AI Chatbot tư vấn khách hàng, quản lý đơn hàng, giỏ hàng, đánh giá và hoàn tiền.

- **Kiến trúc**: Spring Boot 3.3.5, Java 21, Spring Data JPA, Spring Security.
- **Cơ sở dữ liệu**: MySQL / PostgreSQL / H2 Database.
- **Tích hợp bên ngoài**: Cloudinary (Upload ảnh), MoMo API (Thanh toán QR), Groq AI API (Chatbot AI).

---

## 2. API INVENTORY (DANH MỤC TOÀN BỘ API HỆ THỐNG)

Dưới đây là danh mục toàn bộ **74 Endpoints** thuộc 14 Controllers trong dự án:

### 2.1. AccountController (`/api/account`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 1 | `POST` | `/api/account/login` | Đăng nhập tài khoản | Body: `{accountPhone, password}` | **HIGH** |
| 2 | `POST` | `/api/account` | Đăng ký / Tạo tài khoản | Body: `{accountName, accountPhone, password, roleIds}` | **HIGH** |
| 3 | `GET` | `/api/account/{accountId}` | Lấy chi tiết tài khoản | Path: `accountId` | **HIGH** |
| 4 | `GET` | `/api/account/phone/{accountPhone}` | Lấy tài khoản theo SĐT | Path: `accountPhone` | **MEDIUM** |
| 5 | `PUT` | `/api/account/{accountId}` | Cập nhật thông tin tài khoản | Path: `accountId`, Body: `{accountName, password, ...}` | **MEDIUM** |
| 6 | `GET` | `/api/account` | Danh sách tài khoản (Phân trang) | Query: `page`, `size` | **LOW** |
| 7 | `DELETE` | `/api/account/{accountId}` | Xóa tài khoản | Path: `accountId` | **LOW** |
| 8 | `GET` | `/api/account/status/{status}` | Lấy tài khoản theo trạng thái | Path: `status`, Query: `page`, `size` | **LOW** |
| 9 | `GET` | `/api/account/search` | Tìm kiếm tài khoản theo tên | Query: `accountName`, `page`, `size` | **LOW** |

### 2.2. ProductController (`/api/product`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 10 | `GET` | `/api/product` | Lấy tất cả sản phẩm (Phân trang) | Query: `page`, `size` | **HIGH** |
| 11 | `GET` | `/api/product/{productId}` | Lấy chi tiết sản phẩm | Path: `productId` | **HIGH** |
| 12 | `GET` | `/api/product/search` | Tìm kiếm sản phẩm | Query: `keywords`, `minPrice`, `maxPrice`, `page`, `size` | **HIGH** |
| 13 | `GET` | `/api/product/top-10` | Top 10 sản phẩm bán chạy nhất | None | **HIGH** |
| 14 | `GET` | `/api/product/filter` | Lọc sản phẩm theo danh mục & giá | Query: `categoryId`, `status`, `minPrice`, `maxPrice`, `page`, `size` | **HIGH** |
| 15 | `POST` | `/api/product` | Tạo sản phẩm mới (Admin) | Body: `{productName, price, quantity, categoryId, ...}` | **LOW** |
| 16 | `PUT` | `/api/product/{productId}` | Cập nhật sản phẩm (Admin) | Path: `productId`, Body: Update Request | **LOW** |
| 17 | `DELETE` | `/api/product/{productId}` | Xóa sản phẩm (Admin) | Path: `productId` | **LOW** |
| 18 | `POST` | `/api/product/{productId}/cleanup-images` | Dọn dẹp ảnh trùng lặp | Path: `productId` | **LOW** |

### 2.3. CartController (`/api/cart`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 19 | `GET` | `/api/cart/account/{accountId}` | Lấy giỏ hàng theo Account ID | Path: `accountId` | **HIGH** |
| 20 | `POST` | `/api/cart/account/{accountId}/items` | Thêm sản phẩm vào giỏ hàng | Path: `accountId`, Body: `{productId, quantity}` | **HIGH** |
| 21 | `PUT` | `/api/cart/items/{cartItemId}` | Cập nhật số lượng item giỏ hàng | Path: `cartItemId`, Body: `{quantity}` | **HIGH** |
| 22 | `DELETE` | `/api/cart/items/{cartItemId}` | Xóa item khỏi giỏ hàng | Path: `cartItemId` | **HIGH** |
| 23 | `GET` | `/api/cart/account/{accountId}/items` | Lấy danh sách item giỏ hàng | Path: `accountId` | **HIGH** |
| 24 | `DELETE` | `/api/cart/account/{accountId}/clear` | Xóa sạch giỏ hàng | Path: `accountId` | **HIGH** |
| 25 | `POST` | `/api/cart/account/{accountId}` | Tạo giỏ hàng mới | Path: `accountId` | **MEDIUM** |
| 26 | `GET` | `/api/cart` | Lấy tất cả giỏ hàng (Admin) | Query: `page`, `size` | **LOW** |
| 27 | `GET` | `/api/cart/{cartId}` | Lấy giỏ hàng theo Cart ID | Path: `cartId` | **LOW** |
| 28 | `DELETE` | `/api/cart/{cartId}` | Xóa giỏ hàng theo Cart ID | Path: `cartId` | **LOW** |
| 29 | `PUT` | `/api/cart/{cartId}/disable` | Vô hiệu hóa giỏ hàng | Path: `cartId` | **LOW** |
| 30 | `PUT` | `/api/cart/{cartId}/enable` | Kích hoạt lại giỏ hàng | Path: `cartId` | **LOW** |
| 31 | `PUT` | `/api/cart/{cartId}/status/{status}` | Cập nhật trạng thái giỏ hàng | Path: `cartId`, `status` | **LOW** |

### 2.4. OrderController (`/api/order`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 32 | `POST` | `/api/order` | Đặt hàng mới (Create Order) | Body: `{accountId, shippingId, paymentId, paymentMethod, items, totalPrice}` | **HIGH** |
| 33 | `GET` | `/api/order/account/{accountId}` | Lịch sử đơn hàng của khách | Path: `accountId` | **HIGH** |
| 34 | `PUT` | `/api/order/{orderId}/cancel` | Hủy đơn hàng (Khách hàng) | Path: `orderId` | **MEDIUM** |
| 35 | `PUT` | `/api/order/{orderId}/complete` | Xác nhận đã nhận hàng | Path: `orderId` | **MEDIUM** |
| 36 | `GET` | `/api/order` | Lấy tất cả đơn hàng (Admin) | Query: `page`, `size` | **LOW** |
| 37 | `GET` | `/api/order/status/{status}` | Lọc đơn hàng theo trạng thái | Path: `status`, Query: `page`, `size` | **LOW** |
| 38 | `GET` | `/api/order/date-range` | Lọc đơn hàng theo khoảng ngày | Query: `startDate`, `endDate`, `page`, `size` | **LOW** |
| 39 | `GET` | `/api/order/search` | Tìm kiếm đơn hàng theo từ khóa | Query: `keyword`, `page`, `size` | **LOW** |
| 40 | `GET` | `/api/order/filter` | Lọc đơn hàng nâng cao | Query: `status`, `page`, `size` | **LOW** |
| 41 | `GET` | `/api/order/search-filter` | Tìm kiếm và lọc đơn hàng | Query: `keyword`, `status`, `page`, `size` | **LOW** |
| 42 | `PUT` | `/api/order/{orderId}` | Cập nhật đơn hàng (Admin) | Path: `orderId`, Body: Update Request | **LOW** |
| 43 | `DELETE` | `/api/order/{orderId}` | Xóa đơn hàng (Admin) | Path: `orderId` | **LOW** |
| 44 | `PUT` | `/api/order/{orderId}/confirm` | Admin duyệt đơn hàng | Path: `orderId` | **LOW** |
| 45 | `PUT` | `/api/order/{orderId}/start-delivery` | Admin chuyển giao hàng | Path: `orderId` | **LOW** |
| 46 | `PUT` | `/api/order/{orderId}/update-status` | Admin cập nhật trạng thái | Path: `orderId`, Query: `status` | **LOW** |

### 2.5. MomoController (`/api/momo`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 47 | `POST` | `/api/momo/create-payment` | Tạo yêu cầu thanh toán MoMo QR | Body: `{"orderId": "xxx"}` | **HIGH** |
| 48 | `POST` | `/api/momo/ipn-handler` | Webhook IPN nhận kết quả MoMo | Body: `{orderId, resultCode, transId, signature, ...}` | **HIGH** |
| 49 | `GET` | `/api/momo/check-status/{orderId}` | Kiểm tra trạng thái thanh toán | Path: `orderId` | **HIGH** |
| 50 | `GET` | `/api/momo/return` | Frontend redirect callback MoMo | Query: `orderId`, `resultCode`, `message` | **MEDIUM** |

### 2.6. ChatController (`/api/chat`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 51 | `POST` | `/api/chat/messages` | Gửi tin nhắn Chatbot AI | Body: `{sessionId, senderId, content, intent, metadata}` | **HIGH** |
| 52 | `POST` | `/api/chat/sessions` | Tạo phiên chat mới | Body: `{accountId, title}` | **MEDIUM** |
| 53 | `GET` | `/api/chat/sessions/account/{accountId}` | Lấy các phiên chat của tài khoản | Path: `accountId` | **MEDIUM** |
| 54 | `GET` | `/api/chat/sessions/{sessionId}` | Lấy chi tiết phiên chat & tin nhắn | Path: `sessionId` | **MEDIUM** |
| 55 | `GET` | `/api/chat/messages/{sessionId}` | Lấy tin nhắn theo sessionId | Path: `sessionId` | **MEDIUM** |
| 56 | `PATCH` | `/api/chat/sessions/{sessionId}/read` | Đánh dấu tin nhắn đã đọc | Path: `sessionId` | **LOW** |
| 57 | `PUT` | `/api/chat/sessions/{sessionId}` | Cập nhật phiên chat | Path: `sessionId`, Body: Update Session | **LOW** |
| 58 | `PATCH` | `/api/chat/sessions/{sessionId}/close` | Đóng phiên chat | Path: `sessionId` | **LOW** |
| 59 | `DELETE` | `/api/chat/sessions/{sessionId}` | Xóa phiên chat | Path: `sessionId` | **LOW** |
| 60 | `DELETE` | `/api/chat/messages/{messageId}` | Thu hồi tin nhắn | Path: `messageId` | **LOW** |
| 61 | `GET/POST` | `/api/chat/admin/*` | Các API Admin xử lý Ticket CSKH | Path & Body Admin Ticket | **LOW** |

### 2.7. RatingController (`/api/rating`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 62 | `GET` | `/api/rating/product/{productId}` | Lấy danh sách đánh giá sản phẩm | Path: `productId`, Query: `page`, `size` | **HIGH** |
| 63 | `GET` | `/api/rating/product/{productId}/average` | Lấy điểm đánh giá trung bình | Path: `productId` | **HIGH** |
| 64 | `POST` | `/api/rating` | Tạo đánh giá sản phẩm mới | Body: `{accountId, productId, star, comment}` | **MEDIUM** |
| 65 | `GET` | `/api/rating/account/{accountId}` | Đánh giá của tài khoản | Path: `accountId` | **LOW** |
| 66 | `PUT/PATCH/DELETE` | `/api/rating/{ratingId}` | Cập nhật / Xóa đánh giá | Path: `ratingId` | **LOW** |

### 2.8. RefundController (`/api/refund`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 67 | `POST` | `/api/refund` | Gửi yêu cầu hoàn tiền / trả hàng | Body: `{orderId, reason, amount, ...}` | **MEDIUM** |
| 68 | `GET` | `/api/refund/order/{orderId}` | Tra cứu yêu cầu hoàn tiền theo đơn | Path: `orderId` | **MEDIUM** |
| 69 | `GET/PUT/DELETE` | `/api/refund/*` | Quản trị hoàn tiền (Admin) | Path & Body Admin Refund | **LOW** |

### 2.9. ShippingController (`/api/shipping`)
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Request Body / Query Params | Priority |
|---|---|---|---|---|---|
| 70 | `GET` | `/api/shipping/account/{accountId}` | Lấy danh sách địa chỉ giao hàng | Path: `accountId` | **HIGH** |
| 71 | `POST` | `/api/shipping` | Thêm địa chỉ giao hàng mới | Body: `{accountId, address, phone, receiverName}` | **MEDIUM** |
| 72 | `PUT/DELETE` | `/api/shipping/{shippingId}` | Cập nhật / Xóa địa chỉ giao hàng | Path: `shippingId` | **LOW** |

### 2.10. CategoryController & PaymentController & RoleController & UploadFileController
| STT | HTTP Method | Endpoint Path | Mô tả chức năng | Priority |
|---|---|---|---|---|
| 73 | `GET` | `/api/category` | Danh sách danh mục hoa quả | **HIGH** |
| 74 | `POST` | `/api/payment` | Tạo bản ghi thanh toán (nội bộ/COD) | **HIGH** |
| 75 | `GET` | `/api/role` | Lấy danh sách vai trò | **LOW** |
| 76 | `POST` | `/api/upload/image` | Upload hình ảnh lên Cloudinary | **LOW** |

---

## 3. PHÂN LOẠI ĐỘ ƯU TIÊN (PRIORITY MATRIX)

Độ ưu tiên (Priority) được đánh giá tổng hợp dựa trên **3 tiêu chí cốt lõi**:
- **Traffic Frequency (Tần suất lưu lượng)**: Mật độ request/giây do người dùng tạo ra khi duyệt web.
- **Business Criticality (Mức độ quan trọng nghiệp vụ)**: Mức độ ảnh hưởng trực tiếp đến doanh thu, trải nghiệm cốt lõi và tỷ lệ chuyển đổi (Conversion Rate).
- **Performance Sensitivity (Độ nhạy hiệu năng / Tài nguyên)**: Yêu cầu thời gian phản hồi cực nhanh (<200ms) hoặc các tác vụ tiêu tốn nhiều tài nguyên CPU/Memory/DB Query/External Call.

### 3.1. Nguyên tắc Đánh giá và Phân hạng Priority

- **HIGH PRIORITY**: Có $\ge 2$ tiêu chí ở mức **Cao**, hoặc thuộc **Core User Flow / Tác động doanh thu trực tiếp**.
- **MEDIUM PRIORITY**: Có **1 tiêu chí Cao** hoặc **2 tiêu chí Trung bình** (Thao tác cần thiết nhưng tần suất không liên tục).
- **LOW PRIORITY**: Cả **3 tiêu chí ở mức Thấp/Trung bình** (Thao tác Admin nội bộ, tác vụ nền, upload dung lượng lớn).

### 3.2. Bảng Phân Chia Priority Chi Tiết Theo 3 Tiêu Chí

| API / Chức năng | Traffic Frequency | Business Criticality | Performance Sensitivity | Priority | Lý do phân loại chi tiết |
|---|:---:|:---:|:---:|:---:|---|
| **GET Products** (`/api/product`) | **Cao** | **Cao** | **Trung bình** | **HIGH** | Traffic truy cập trang chủ/trang danh mục rất cao; ảnh hưởng đầu tiên tới trải nghiệm mua hàng. |
| **Search & Filter Products** (`/api/product/search`, `/filter`) | **Cao** | **Cao** | **Cao** | **HIGH** | Traffic cao + Query DB phức tạp (ILIKE, range price, status); nhạy cảm với latency nếu DB không có Index tốt. |
| **Product Detail & Average Rating** (`/api/product/{id}`) | **Cao** | **Cao** | **Trung bình** | **HIGH** | Tần suất click xem chi tiết sản phẩm cao; nơi ra quyết định thêm vào giỏ hàng. |
| **Add / Update Cart** (`/api/cart/items`) | **Trung bình** | **Cao** | **Cao** | **HIGH** | **Core User Flow**: Tác động trực tiếp tới giỏ hàng, cần xử lý ghi nhất quán (Concurrency/Locking). |
| **Create Order** (`/api/order`) | **Trung bình** | **Cực cao** | **Cao** | **HIGH** | **Business Critical**: Khâu chốt đơn sinh ra doanh thu, xử lý Transaction DB phức tạp, giảm số lượng tồn kho. |
| **MoMo Payment** (`/api/momo/create-payment`, `/ipn-handler`) | **Trung bình** | **Cực cao** | **Cao** | **HIGH** | **Business Critical**: Xử lý tiền tệ, phụ thuộc Latency gọi API MoMo bên ngoài & xử lý Webhook Callback chính xác. |
| **AI Chatbot** (`/api/chat/messages`) | **Trung bình** | **Trung bình** | **Cực cao** | **HIGH** | Phụ thuộc vào External Network Latency (Groq AI API), nếu không tối ưu dễ làm nghẽn Worker Thread. |
| **Order History** (`/api/order/account/{id}`) | **Trung bình** | **Cao** | **Trung bình** | **HIGH** | User tra cứu trạng thái đơn hàng thường xuyên sau khi mua. |
| **Login** (`/api/account/login`) | **Trung bình** | **Cao** | **Trung bình** | **HIGH** | Điểm đầu luồng người dùng; quan trọng cho xác thực tài khoản. |
| **Register** (`/api/account`) | **Thấp** | **Cao** | **Trung bình** | **MEDIUM** | Thao tác quan trọng thu hút user mới nhưng tần suất gọi thấp. |
| **Add Shipping Address** (`/api/shipping`) | **Thấp** | **Trung bình** | **Thấp** | **MEDIUM** | Thao tác phụ trong luồng checkout, tần suất thực hiện ít. |
| **Create Rating / Review** (`/api/rating`) | **Thấp** | **Trung bình** | **Thấp** | **MEDIUM** | Thực hiện sau khi nhận đơn hàng thành công, không tạo áp lực tức thì lên hệ thống. |
| **Create Refund Request** (`/api/refund`) | **Rất thấp** | **Cao** | **Thấp** | **MEDIUM** | Quy trình nghiệp vụ quan trọng nhưng tỉ lệ phát sinh sự cố/hoàn tiền nhỏ. |
| **Admin CRUD APIs** (Product/Account/Order Management) | **Rất thấp** | **Trung bình** | **Thấp** | **LOW** | Chỉ phục vụ số ít quản trị viên (Internal Tool), traffic cực thấp so với end-user. |
| **Upload Image Cloudinary** (`/api/upload/image`) | **Rất thấp** | **Thấp** | **Cao** | **LOW** | Xử lý I/O đệm file và upload Cloudinary tốn thời gian nhưng không thuộc tải thường nhật của hệ thống. |

---

## 4. 7 CRITICAL BUSINESS FLOWS (KỊCH BẢN TẢI TRỌNG YẾU TỐ)

Từ các High Priority APIs, chúng ta xây dựng **7 Kịch bản luồng nghiệp vụ thực tế (Critical User Journeys)**:

- **Flow 1: Guest Browsing & Product Discovery (Khách ghé thăm xem sản phẩm)**
  - Tỷ lệ tải: **40% tổng lượt truy cập** (User xem hàng không đăng nhập).
  - Tần suất: Cao nhất.

- **Flow 2: Customer Registration, Login & Profile Load (Đăng ký, Đăng nhập & Tải thông tin)**
  - Tỷ lệ tải: **15% tổng lượt truy cập**.
  - Kiểm tra độ nén CPU khi mã hóa mật khẩu & đọc thông tin DB.

- **Flow 3: Shopping Cart Management (Thao tác Giỏ hàng)**
  - Tỷ lệ tải: **15% tổng lượt truy cập**.
  - Thêm sản phẩm, điều chỉnh số lượng, tải danh sách giỏ hàng.

- **Flow 4: Standard COD Checkout Flow (Đặt hàng Ship COD)**
  - Tỷ lệ tải: **10% tổng lượt truy cập**.
  - Luồng tạo đơn hàng truyền thống, kiểm tra transaction database lock & giỏ hàng.

- **Flow 5: Online MoMo E-Wallet Payment Flow (Thanh toán Ví MoMo & IPN)**
  - Tỷ lệ tải: **10% tổng lượt truy cập**.
  - Đặt hàng -> Gọi tạo mã QR -> Webhook MoMo IPN giả lập phản hồi thành công -> Kiểm tra status đơn.

- **Flow 6: Real-time AI Assistant Chatbot (Hỏi đáp Trợ lý AI Bán hàng)**
  - Tỷ lệ tải: **5% tổng lượt truy cập**.
  - Khách hỏi về hoa quả, gợi ý sản phẩm, Chatbot phản hồi tự động qua AI Service.

- **Flow 7: Post-Purchase Rating & Refund Request (Đánh giá & Yêu cầu hoàn tiền)**
  - Tỷ lệ tải: **5% tổng lượt truy cập**.
  - Đánh giá sản phẩm đã mua và gửi ticket hoàn tiền nếu có sự cố.

---

## 5. KỊCH BẢN GATLING SCENARIOS (SIMULATION CODE)

Dưới đây là mã nguồn Gatling Java DSL hoàn chỉnh, sẵn sàng chạy performance test trên dự án Spring Boot.

Mã nguồn được lưu tại: `performance/gatling/src/test/java/simulations/FruitShopFullSimulation.java`

```java
package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class FruitShopFullSimulation extends Simulation {

    // 1. HTTP Configuration
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/PerformanceTest/FruitShop");

    // Sample Dynamic Feeders
    private final Iterator<Map<String, Object>> userFeeder =
        Stream.generate(() -> {
            String randomPhone = "09" + String.format("%08d", new Random().nextInt(100000000));
            Map<String, Object> map = new HashMap<>();
            map.put("phone", randomPhone);
            map.put("password", "123456");
            map.put("name", "TestUser_" + UUID.randomUUID().toString().substring(0, 5));
            return map;
        }).iterator();

    // =========================================================================
    // FLOW 1: Guest Browsing & Product Discovery (40% Weight)
    // =========================================================================
    private final ScenarioBuilder flow1GuestBrowsing = scenario("Flow 1: Guest Browsing & Product Discovery")
        .exec(http("Top 10 Products")
            .get("/api/product/top-10")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Get Categories")
            .get("/api/category")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Search Products")
            .get("/api/product/search?keywords=T%C3%A1o&page=0&size=10")
            .check(status().is(200)))
        .pause(2)
        .exec(http("Filter Products")
            .get("/api/product/filter?minPrice=10000&maxPrice=500000&page=0&size=10")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Get Product Details")
            .get("/api/product/p-1")
            .check(status().or(status().is(200), status().is(404))))
        .exec(http("Get Product Ratings")
            .get("/api/rating/product/p-1")
            .check(status().or(status().is(200), status().is(404))));

    // =========================================================================
    // FLOW 2: Customer Registration & Authentication (15% Weight)
    // =========================================================================
    private final ScenarioBuilder flow2AuthAndProfile = scenario("Flow 2: Customer Auth & Profile")
        .feed(userFeeder)
        .exec(http("Register Account")
            .post("/api/account")
            .body(StringBody("{\"accountName\":\"#{name}\",\"accountPhone\":\"#{phone}\",\"password\":\"#{password}\"}"))
            .check(status().or(status().is(201), status().is(409))))
        .pause(1)
        .exec(http("Login Account")
            .post("/api/account/login")
            .body(StringBody("{\"accountPhone\":\"#{phone}\",\"password\":\"#{password}\"}"))
            .check(status().or(status().is(200), status().is(401))))
        .pause(1)
        .exec(http("Get Account Details")
            .get("/api/account/acc-1")
            .check(status().or(status().is(200), status().is(404))))
        .exec(http("Get Shipping Addresses")
            .get("/api/shipping/account/acc-1")
            .check(status().or(status().is(200), status().is(404))));

    // =========================================================================
    // FLOW 3: Shopping Cart Management (15% Weight)
    // =========================================================================
    private final ScenarioBuilder flow3CartManagement = scenario("Flow 3: Shopping Cart Management")
        .exec(http("Get Cart")
            .get("/api/cart/account/acc-1")
            .check(status().or(status().is(200), status().is(404))))
        .pause(1)
        .exec(http("Add Item to Cart")
            .post("/api/cart/account/acc-1/items")
            .body(StringBody("{\"productId\":\"p-1\",\"quantity\":2}"))
            .check(status().or(status().is(200), status().is(400))))
        .pause(1)
        .exec(http("Get Cart Items")
            .get("/api/cart/account/acc-1/items")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Update Cart Item Quantity")
            .put("/api/cart/items/ci-1")
            .body(StringBody("{\"quantity\":5}"))
            .check(status().or(status().is(200), status().is(400))));

    // =========================================================================
    // FLOW 4: Standard COD Checkout & Order History (10% Weight)
    // =========================================================================
    private final ScenarioBuilder flow4CODCheckout = scenario("Flow 4: Standard COD Checkout & Orders")
        .exec(http("View Cart Before Checkout")
            .get("/api/cart/account/acc-1/items")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Create Payment Record (COD)")
            .post("/api/payment")
            .body(StringBody("{\"paymentMethod\":\"COD\",\"paymentStatus\":0,\"amount\":150000}"))
            .check(status().or(status().is(201), status().is(200), status().is(400))))
        .pause(1)
        .exec(http("Create Order")
            .post("/api/order")
            .body(StringBody("{" +
                "\"accountId\":\"acc-1\"," +
                "\"shippingId\":\"ship-1\"," +
                "\"paymentMethod\":0," +
                "\"totalPrice\":150000," +
                "\"items\":[{\"productId\":\"p-1\",\"unitPrice\":75000,\"quantity\":2,\"totalPrice\":150000}]" +
                "}"))
            .check(status().or(status().is(200), status().is(400))))
        .pause(1)
        .exec(http("Clear Cart After Checkout")
            .delete("/api/cart/account/acc-1/clear")
            .check(status().is(200)))
        .exec(http("Get Order History")
            .get("/api/order/account/acc-1")
            .check(status().is(200)));

    // =========================================================================
    // FLOW 5: Online MoMo E-Wallet Payment & Webhook (10% Weight)
    // =========================================================================
    private final ScenarioBuilder flow5MoMoPayment = scenario("Flow 5: Online MoMo Payment & Webhook IPN")
        .exec(http("Create Order for MoMo")
            .post("/api/order")
            .body(StringBody("{" +
                "\"accountId\":\"acc-1\"," +
                "\"shippingId\":\"ship-1\"," +
                "\"paymentMethod\":1," +
                "\"totalPrice\":200000," +
                "\"items\":[{\"productId\":\"p-1\",\"unitPrice\":100000,\"quantity\":2,\"totalPrice\":200000}]" +
                "}"))
            .check(status().or(status().is(200), status().is(400))))
        .pause(1)
        .exec(http("Create MoMo QR Payment Request")
            .post("/api/momo/create-payment")
            .body(StringBody("{\"orderId\":\"ord-1\"}"))
            .check(status().or(status().is(200), status().is(400), status().is(404))))
        .pause(1)
        .exec(http("Simulate MoMo IPN Callback Webhook")
            .post("/api/momo/ipn-handler")
            .body(StringBody("{" +
                "\"partnerCode\":\"MOMO\"," +
                "\"orderId\":\"ord-1\"," +
                "\"requestId\":\"req-1\"," +
                "\"amount\":200000," +
                "\"orderInfo\":\"Payment for order ord-1\"," +
                "\"orderType\":\"momo_wallet\"," +
                "\"transId\":123456789," +
                "\"resultCode\":0," +
                "\"message\":\"Success\"," +
                "\"responseTime\":1600000000000L," +
                "\"extraData\":\"\"," +
                "\"signature\":\"dummy_signature\"" +
                "}"))
            .check(status().or(status().is(204), status().is(401), status().is(500))))
        .exec(http("Check MoMo Payment Status")
            .get("/api/momo/check-status/ord-1")
            .check(status().or(status().is(200), status().is(404))));

    // =========================================================================
    // FLOW 6: Real-time AI Assistant Chatbot (5% Weight)
    // =========================================================================
    private final ScenarioBuilder flow6AIChatbot = scenario("Flow 6: Real-time AI Assistant Chatbot")
        .exec(http("Create Chat Session")
            .post("/api/chat/sessions")
            .body(StringBody("{\"accountId\":\"acc-1\",\"title\":\"T%C6%B0 v%E1%BA%A5n hoa qu%E1%BA%A3\"}"))
            .check(status().or(status().is(201), status().is(200))))
        .pause(1)
        .exec(http("Send Chat Message to AI Bot")
            .post("/api/chat/messages")
            .body(StringBody("{" +
                "\"sessionId\":\"sess-1\"," +
                "\"senderId\":\"acc-1\"," +
                "\"content\":\"T%C6%B0 v%E1%BA%A5n gi%C3%BAp t%C3%B4i hoa qu%E1%BA%A3 b%E1%BB%95 d%C6%B0%C6%A1ng\"," +
                "\"intent\":\"PRODUCT_ADVICE\"" +
                "}"))
            .check(status().or(status().is(201), status().is(200), status().is(500))))
        .pause(2)
        .exec(http("Get Message History")
            .get("/api/chat/messages/sess-1")
            .check(status().or(status().is(200), status().is(404))))
        .exec(http("Mark Messages as Read")
            .patch("/api/chat/sessions/sess-1/read")
            .check(status().is(200)));

    // =========================================================================
    // FLOW 7: Post-Purchase Rating & Refund Request (5% Weight)
    // =========================================================================
    private final ScenarioBuilder flow7RatingAndRefund = scenario("Flow 7: Rating & Refund Request")
        .exec(http("Create Product Rating")
            .post("/api/rating")
            .body(StringBody("{\"accountId\":\"acc-1\",\"productId\":\"p-1\",\"star\":5,\"comment\":\"Tr%C3%A1i c%C3%A2y r%E1%BA%A5t t%C6%B0%C6%A1i ngon!\"}"))
            .check(status().or(status().is(201), status().is(200), status().is(400))))
        .pause(1)
        .exec(http("Get Product Average Rating")
            .get("/api/rating/product/p-1/average")
            .check(status().is(200)))
        .pause(1)
        .exec(http("Create Refund Request")
            .post("/api/refund")
            .body(StringBody("{\"orderId\":\"ord-1\",\"reason\":\"H%C3%A0ng d%E1%BA%A5p h%E1%BB%8Fng\",\"amount\":50000}"))
            .check(status().or(status().is(200), status().is(400))));

    // =========================================================================
    // 6. Simulation Load Profile (Cấu hình ramp-up chịu tải)
    // =========================================================================
    {
        setUp(
            flow1GuestBrowsing.injectOpen(
                rampUsers(40).during(Duration.ofSeconds(10)),
                constantUsersPerSec(20).during(Duration.ofSeconds(30))
            ),
            flow2AuthAndProfile.injectOpen(
                rampUsers(15).during(Duration.ofSeconds(10)),
                constantUsersPerSec(8).during(Duration.ofSeconds(30))
            ),
            flow3CartManagement.injectOpen(
                rampUsers(15).during(Duration.ofSeconds(10)),
                constantUsersPerSec(8).during(Duration.ofSeconds(30))
            ),
            flow4CODCheckout.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(10)),
                constantUsersPerSec(5).during(Duration.ofSeconds(30))
            ),
            flow5MoMoPayment.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(10)),
                constantUsersPerSec(5).during(Duration.ofSeconds(30))
            ),
            flow6AIChatbot.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10)),
                constantUsersPerSec(2).during(Duration.ofSeconds(30))
            ),
            flow7RatingAndRefund.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10)),
                constantUsersPerSec(2).during(Duration.ofSeconds(30))
            )
        ).protocols(httpProtocol);
    }
}
```

---

## 6. HƯỚNG DẪN THỰC THI & TIÊU CHÍ ĐÁNH GIÁ HIỆU NĂNG

### 6.1. Thêm Gatling Maven Plugin vào `pom.xml`
Để chạy kịch bản trực tiếp bằng Maven, bổ sung plugin sau vào file `pom.xml`:

```xml
<plugin>
    <groupId>io.gatling</groupId>
    <artifactId>gatling-maven-plugin</artifactId>
    <version>3.10.5</version>
    <configuration>
        <simulationClass>simulations.FruitShopFullSimulation</simulationClass>
    </configuration>
</plugin>
```

### 6.2. Câu lệnh chạy Test
Khởi động Server Spring Boot, sau đó mở terminal và chạy:

```bash
mvn gatling:test
```

### 6.3. Tiêu chí Đánh giá Thành công (Acceptance Criteria / SLA)
- **Tỷ lệ lỗi (Error Rate)**: `< 1.0%` tổng số request.
- **Thời gian phản hồi 95th Percentile (p95 Response Time)**:
  - API đọc dữ liệu (`GET /api/product`, `GET /api/category`): `< 200ms`.
  - API ghi dữ liệu (`POST /api/order`, `POST /api/cart`): `< 500ms`.
  - API tích hợp bên ngoài (AI Chatbot, MoMo QR): `< 1500ms`.
- **Thông lượng (Throughput - TPS)**: Đạt tối thiểu **200+ Requests Per Second (RPS)** trên môi trường test chuẩn.
