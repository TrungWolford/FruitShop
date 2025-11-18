# FruitShop 🍎🛒

E-commerce platform for fruit shop with admin management and MoMo payment integration.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 13+
- pnpm (for frontend)

### Setup & Run

#### 1. Backend (Spring Boot)
```bash
cd server
./mvnw spring-boot:run
# Server runs on http://localhost:8080
```

#### 2. Frontend (React + Vite)
```bash
cd client
pnpm install
pnpm run dev
# Frontend runs on http://localhost:5173
```

#### 3. Database
```bash
# Create PostgreSQL database
createdb -U admin FruitShop

# Connection details in server/src/main/resources/application.properties
```

---

## 💳 MoMo Payment Integration

### ⚠️ Important: Development Setup Required

For MoMo payment to work in development, you need to expose your localhost backend to the internet using **ngrok** so MoMo can send IPN (payment notification) callbacks.

### Quick Setup with ngrok

**Option 1: Automated (Recommended)**
```powershell
# Terminal 1: Start ngrok
ngrok http 8080

# Terminal 2: Run setup script
.\setup-momo-dev.ps1

# Terminal 3: Restart backend
cd server
.\mvnw spring-boot:run
```

**Option 2: Manual**
```powershell
# 1. Start ngrok
ngrok http 8080
# Copy the https URL (e.g., https://abc123.ngrok-free.app)

# 2. Edit server/src/main/resources/application.properties
# Update: momo.ipn-url=https://YOUR-NGROK-URL/api/momo/ipn-handler

# 3. Restart backend
cd server
.\mvnw spring-boot:run
```

**Verify ngrok is working:**
- Open http://localhost:4040 (ngrok web UI)
- After a payment, you should see POST requests to `/api/momo/ipn-handler`

### 📚 Detailed Documentation
- **[MOMO_ISSUE_ROOT_CAUSE.md](./MOMO_ISSUE_ROOT_CAUSE.md)** - Why ngrok is needed
- **[SETUP_MOMO_DEV_GUIDE.md](./SETUP_MOMO_DEV_GUIDE.md)** - Step-by-step setup
- **[MOMO_PAYMENT_STATUS_DEBUG_GUIDE.md](./MOMO_PAYMENT_STATUS_DEBUG_GUIDE.md)** - Troubleshooting

---

## 📂 Project Structure

```
FruitShop/
├── client/                 # React frontend
│   ├── src/
│   │   ├── pages/
│   │   │   └── Admin/
│   │   │       ├── AdminPayment.tsx    # Payment management UI
│   │   │       └── AdminOrder.tsx       # Order management UI
│   │   ├── services/
│   │   │   ├── paymentService.ts       # Payment API calls
│   │   │   └── orderService.ts         # Order API calls
│   │   └── components/
│   └── package.json
│
├── server/                 # Spring Boot backend
│   ├── src/main/java/server/FruitShop/
│   │   ├── controller/
│   │   │   ├── MomoController.java     # MoMo payment endpoints
│   │   │   └── OrderController.java    # Order endpoints
│   │   ├── service/
│   │   │   ├── MomoService.java        # MoMo integration
│   │   │   └── Impl/
│   │   │       └── OrderServiceImpl.java  # Order business logic
│   │   ├── entity/
│   │   │   ├── Payment.java            # Payment entity
│   │   │   └── Order.java              # Order entity
│   │   └── repository/
│   └── pom.xml
│
├── setup-momo-dev.ps1      # Automated ngrok setup script
└── *.md                    # Documentation files
```

---

## 🔧 Features

### Customer Features
- Browse products
- Add to cart
- Place orders
- MoMo QR payment
- Order history
- Payment status tracking

### Admin Features
- **Order Management**
  - View all orders with filters
  - Update order status
  - Auto-update payment status when order completed
- **Payment Management** 
  - View all payments
  - Search and filter payments
  - Update payment status manually
  - Track MoMo transactions
- Product management
- User management

---

## 🔄 Payment Status Auto-Update

### Automatic Status Updates

**1. When Admin Completes Order:**
```
Admin clicks "Complete Order"
    ↓
Order status → 4 (Completed)
Payment status → 1 (Completed) ✅ Auto-updated
```

**2. When MoMo Payment Succeeds:**
```
User pays via MoMo
    ↓
MoMo IPN callback → Backend
    ↓
Payment status → 1 (Completed) ✅ Auto-updated
Order status → 2 (Confirmed)
```

**3. When MoMo Payment Fails:**
```
Payment fails/timeout
    ↓
MoMo IPN callback → Backend
    ↓
Payment status → 2 (Failed) ✅ Auto-updated
Order status → 0 (Cancelled)
```

### Payment Status Values
| Status | Value | Description |
|--------|-------|-------------|
| Pending | 0 | Chờ xử lý - Waiting for payment |
| Completed | 1 | Đã thanh toán - Payment successful |
| Failed | 2 | Thất bại - Payment failed |
| Refunded | 3 | Hoàn tiền - Refunded |

### Documentation
- **[PAYMENT_AUTO_UPDATE_COMPLETE.md](./PAYMENT_AUTO_UPDATE_COMPLETE.md)** - Full implementation docs
- **[PAYMENT_MANAGEMENT_COMPLETE_GUIDE.md](./PAYMENT_MANAGEMENT_COMPLETE_GUIDE.md)** - User guide
- **[QUICK_REFERENCE_PAYMENT_AUTO_UPDATE.md](./QUICK_REFERENCE_PAYMENT_AUTO_UPDATE.md)** - Quick reference

---

## 🧪 Testing

### Test Payment Flow
1. Create an order from customer interface
2. Generate MoMo QR code
3. Pay using MoMo app (sandbox)
4. Verify in Admin Panel:
   - Payment status = "Đã thanh toán"
   - Order status = "Đã xác nhận"

### Test MoMo Sandbox
- Use MoMo test credentials in `application.properties`
- Test phone: Use your own MoMo account (sandbox mode)
- Amount: Any amount (will be simulated)

### Check Logs
```bash
# Backend logs should show:
📨 Received MoMo IPN callback
✅ Payment successful for orderId: xxx
✅ Payment status updated to COMPLETED (1) via MoMo
```

---

## 🐛 Troubleshooting

### Payment Status Not Updating?
**Problem:** Status stays at "Chờ xử lý" after MoMo payment

**Solution:** Your backend needs to be accessible from internet
1. Check ngrok is running: `ngrok http 8080`
2. Verify IPN URL in application.properties uses ngrok URL
3. Monitor ngrok web UI: http://localhost:4040
4. See **[MOMO_ISSUE_ROOT_CAUSE.md](./MOMO_ISSUE_ROOT_CAUSE.md)** for details

### Common Issues
- **404 on IPN:** Backend not running on port 8080
- **Signature error:** Wrong SECRET_KEY in config
- **Timeout:** ngrok URL expired (restart ngrok)

---

## 📖 Documentation Index

### Setup & Configuration
- [SETUP_MOMO_DEV_GUIDE.md](./SETUP_MOMO_DEV_GUIDE.md) - Development setup
- [MOMO_ISSUE_ROOT_CAUSE.md](./MOMO_ISSUE_ROOT_CAUSE.md) - Why ngrok is needed

### Feature Documentation
- [PAYMENT_AUTO_UPDATE_COMPLETE.md](./PAYMENT_AUTO_UPDATE_COMPLETE.md) - Payment auto-update
- [ADMIN_PAYMENT_UI_DOCUMENTATION.md](./ADMIN_PAYMENT_UI_DOCUMENTATION.md) - Admin payment UI
- [PAYMENT_MANAGEMENT_COMPLETE_GUIDE.md](./PAYMENT_MANAGEMENT_COMPLETE_GUIDE.md) - Complete guide

### Testing & Debug
- [PAYMENT_STATUS_AUTO_UPDATE_TESTING_CHECKLIST.md](./PAYMENT_STATUS_AUTO_UPDATE_TESTING_CHECKLIST.md) - Test checklist
- [MOMO_PAYMENT_STATUS_DEBUG_GUIDE.md](./MOMO_PAYMENT_STATUS_DEBUG_GUIDE.md) - Debug guide

### Quick Reference
- [QUICK_REFERENCE_PAYMENT_AUTO_UPDATE.md](./QUICK_REFERENCE_PAYMENT_AUTO_UPDATE.md) - Quick reference
- [PAYMENT_STATUS_AUTO_UPDATE_SUMMARY.md](./PAYMENT_STATUS_AUTO_UPDATE_SUMMARY.md) - Summary

---

## 🛠️ Tech Stack

### Frontend
- React 18
- TypeScript
- Vite
- TailwindCSS
- React Router
- Zustand (state management)

### Backend
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Lombok
- MoMo Payment API

### Development Tools
- ngrok (for MoMo development)
- Maven
- pnpm

---

## 🔐 Environment Variables

### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5434/FruitShop
spring.datasource.username=admin
spring.datasource.password=123

# MoMo
momo.partner-code=MOMO
momo.access-key=F8BBA842ECF85
momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
momo.ipn-url=https://YOUR-NGROK-URL/api/momo/ipn-handler  # Update this!
momo.return-url=http://localhost:5173/customer/orders
```

---

## 📝 API Endpoints

### MoMo Payment
- `POST /api/momo/create-payment` - Create payment QR
- `POST /api/momo/ipn-handler` - IPN callback (from MoMo)
- `GET /api/momo/return` - Return URL callback
- `GET /api/momo/check-status/{orderId}` - Check payment status

### Admin Payment Management
- `GET /api/admin/payments` - List all payments
- `GET /api/admin/payments/{id}` - Get payment details
- `PUT /api/admin/payments/{id}` - Update payment status

### Orders
- `GET /api/orders` - List orders
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/complete` - Complete order (auto-updates payment)

---

## 👥 Contributors

- Development Team
- Product Owner

---

## 📄 License

This project is proprietary software. All rights reserved.

---

## 🎯 Getting Help

1. Check documentation files (see Documentation Index above)
2. Check logs: Backend console + ngrok UI
3. Review test checklist
4. Debug guide for common issues

**Happy coding! 🚀**