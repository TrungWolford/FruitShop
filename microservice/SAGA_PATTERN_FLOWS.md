# Saga Pattern Flows - FruitShop Microservices

## 1. Architecture Decision: Pure Choreography

### Current Recommendation: **Pure Choreography Approach**
- **Choreography** (Event-driven): All services act autonomously by subscribing to and publishing events. There is no central orchestrator. 
- **Reason**: Keeps the architecture simple, heavily decoupled, and avoids a single point of failure without needing a complex orchestration framework. Every flow (Order, Refund, Cascades) will be handled via event chains.

---

## 2. Core Saga Flows

### 2.1. ORDER SAGA (Priority 1 - Critical)
**Type**: Choreography (linear flow) + Orchestration (failure handling)

#### **Happy Path: Order → Payment → Shipping → Confirmation**

```
┌─────────────────────────────────────────────────────────────────┐
│ CLIENT                                                          │
└──────────────────┬──────────────────────────────────────────────┘
                   │ POST /orders (with items)
                   ▼
        ┌─────────────────────┐
        │  ORDER SERVICE      │ (Sync: Validate Account/Product)
        │  create()           │ ✓ Verify account exists (Feign)
        └──────────┬──────────┘ ✓ Verify product exists (Feign)
                   │ ✓ Save Order (status=PENDING)
                   │ ✓ Save OrderItems
                   ▼
        ┌─────────────────────┐
        │ PUBLISH EVENT       │
        │ OrderCreatedEvent   │
        │ {                   │
        │   orderId,          │
        │   accountId,        │
        │   totalAmount,      │
        │   items,            │
        │   createdAt         │
        │ }                   │
        └──────────┬──────────┘
                   │ (RabbitMQ)
        ┌──────────┴──────────┬──────────────┬──────────────┐
        ▼                     ▼              ▼              ▼
    PAYMENT             CART               NOTIFICATION    ANALYTICS
    SERVICE             SERVICE            SERVICE         SERVICE
    ┌────────────┐  ┌──────────┐
    │ Subscribe  │  │Subscribe │
    │ Event      │  │ Event    │
    └─────┬──────┘  └────┬─────┘
          │ Create        │ Clear cart
          │ Payment       │ for account
          │ (status=      │
          │ PENDING)      │
          ▼               ▼
    ┌────────────┐  ┌──────────┐
    │ PUBLISH    │  │ PUBLISH  │
    │ Payment    │  │ Cart     │
    │ Created    │  │ Cleared  │
    │ Event      │  │ Event    │
    └─────┬──────┘  └────┬─────┘
          │ (Continue to next saga step)
          │
          ▼
    ┌──────────────────┐
    │ PAYMENT SERVICE  │
    │ Process Payment  │
    │ (Call Payment    │
    │  Gateway API)    │
    └────────┬─────────┘
             │ 
        ┌────┴────────────┐
        ▼                 ▼
    [SUCCESS]         [FAILURE]
    Publish            Publish
    Payment            Payment
    Completed          Failed
    Event              Event
        │                 │
        ▼                 ▼
    ORDER-SHIPPING    COMPENSATION
    SERVICE           Start REFUND
    Creates           Saga
    Shipping          (See section 2.3)
        │
        ▼
    ┌─────────────────┐
    │ PUBLISH Event   │
    │ Shipping        │
    │ Created         │
    └────────┬────────┘
             │
             ▼
    ORDER SERVICE
    Updates Order
    status=READY_FOR_PICKUP
             │
             ▼
    ┌──────────────────┐
    │ PUBLISH Event    │
    │ Order Ready      │
    └──────────────────┘
```

#### **Event Sequence Table:**

| Step | Service | Event | Payload | Consumers |
|------|---------|-------|---------|-----------|
| 1 | Order | **OrderCreatedEvent** | orderId, accountId, totalAmount, items | Payment, Cart, Notification, Analytics |
| 2 | Payment | **PaymentCreatedEvent** | paymentId, orderId, amount, status=PENDING | - |
| 3 | Payment | **PaymentCompletedEvent** | paymentId, orderId, amount, transactionId | Order, Shipping |
| 2b (fail) | Payment | **PaymentFailedEvent** | paymentId, orderId, reason, amount | Order, Cart (retry permission), Refund |
| 4 | Shipping | **ShippingCreatedEvent** | shippingId, orderId, address, shippingFee | - |
| 5 | Order | **OrderConfirmedEvent** | orderId, status=READY_FOR_PICKUP | Notification, Analytics |

#### **State Transitions:**

```
Order States:
PENDING → PAYMENT_PROCESSING → PAYMENT_COMPLETED → READY_FOR_PICKUP → SHIPPED → DELIVERED
   │            │                   │
   │        (error)             (error)
   └─────────────┴──────────────────┴──────────────→ CANCELLED

Payment States:
PENDING → PROCESSING → COMPLETED
   │          │
   │      (error)
   └──────────┴──────────────→ FAILED

Shipping States:
PENDING → READY → IN_TRANSIT → DELIVERED
             │        │
         (error)   (error)
             └────────┴──────────→ RETURNED/FAILED
```

#### **Failure Scenarios & Compensation:**

**Scenario A: Payment Processing Fails**
```
1. PaymentFailedEvent published
2. Order Service listens → Updates order status = CANCELLED
3. Cart Service listens → Allows cart to be reused
4. Notification Service → Send "Payment failed" email
5. Refund Saga NOT triggered (no payment to refund)
```

**Scenario B: Shipping Creation Fails**
```
1. ShippingFailedEvent published
2. Order Service listens → Updates order status = PAYMENT_COMPLETED (waiting for manual intervention)
3. Notification Service → Alert admin
4. Manual retry or cancellation with refund
```

---

### 2.2. REFUND SAGA (Priority 2 - Important)
**Type**: Choreography with manual approval step

#### **State Machine:**

```
Refund Request Initiated
│
├─ Check refund eligibility:
│  ├─ Order exists? YES
│  ├─ Order status in [DELIVERED, READY_FOR_PICKUP]? YES
│  ├─ Refund requested within 30 days? YES
│  └─ Within stock/availability? YES
│
▼
┌──────────────────────────────┐
│ OrderService listens to:     │
│ RefundRequestedEvent         │
│ - Update refund record       │
│ - Set status = PENDING       │
│ - Save to DB                 │
└──────────────┬───────────────┘
               │
               ▼
        [ADMIN REVIEW]
        Approve/Reject via:
        PUT /orders/{orderId}/refunds/{refundId}
               │
        ┌──────┴──────┐
        ▼             ▼
    APPROVED      REJECTED
        │             │
        ▼             ▼
    ┌────────────────────────────┐
    │ PUBLISH:                   │
    │ RefundApprovedEvent OR     │
    │ RefundRejectedEvent        │
    └────────┬───────────────────┘
             │
        ┌────┴─────────────────┐
        ▼                      ▼
    APPROVED              REJECTED
    (go to step 1)        (end saga)
        │
        ▼
    ┌────────────────────────┐
    │ PaymentService listens │
    │ RefundApprovedEvent    │
    │                        │
    │ Action:                │
    │ - Create refund record │
    │ - Call Payment Gateway │
    │   to process refund    │
    │ - Save transactionId   │
    └────────┬───────────────┘
             │
        ┌────┴──────────┐
        ▼               ▼
    SUCCESS         FAILURE
        │               │
        ▼               ▼
    Publish         Publish
    RefundCompleted RefundFailed
    Event           Event
        │               │
        ▼               ▼
    ┌─────────────────────────┐
    │ OrderService listens    │
    │ Updates refund record   │
    │ status = COMPLETED OR   │
    │ status = FAILED         │
    └─────────────────────────┘
        │
        ▼
    Notify customer
    of refund status
```

#### **Event Sequence:**

| Step | Service | Event | Payload |
|------|---------|-------|---------|
| 1 | Order | **RefundRequestedEvent** | refundId, orderId, reason, imageUrls, amount |
| 2 | Admin | **RefundApprovedEvent** | refundId, orderId, approverName, approvedAt |
| 3 | Payment | **RefundProcessingEvent** | refundId, paymentId, amount |
| 4a (success) | Payment | **RefundCompletedEvent** | refundId, transactionId, completedAt |
| 4b (failure) | Payment | **RefundFailedEvent** | refundId, reason, failureCode |
| 5 | Order | **RefundStatusUpdatedEvent** | refundId, status, updatedAt |

#### **Compensation Triggers:**

```
IF RefundFailed:
  - Notify admin → Manual retry or cancellation
  - Log error for audit
  - Prevent duplicate refund attempts
  - Set refundStatus = FAILED_PENDING_MANUAL_REVIEW

IF PaymentGatewayUnreachable:
  - Retry with exponential backoff (1, 2, 5, 10 min)
  - If all retries fail: Escalate to admin
```

---

### 2.3. RATING/REVIEW CASCADE (Priority 3 - Medium)
**Type**: Choreography

```
┌────────────────────────────┐
│ ClientPost Review Request  │
└──────────────┬─────────────┘
               │
               ▼
    ┌──────────────────────┐
    │ REVIEW SERVICE       │
    │ createRating()       │
    │ - Validate account   │
    │ - Validate product   │
    │ - Save rating        │
    └────────┬─────────────┘
             │
             ▼
    ┌──────────────────────┐
    │ PUBLISH EVENT:       │
    │ RatingCreatedEvent   │
    │ {                    │
    │   ratingId,          │
    │   productId,         │
    │   rating (1-5),      │
    │   review,            │
    │   createdAt          │
    │ }                    │
    └────────┬─────────────┘
             │ (RabbitMQ)
             ▼
    ┌──────────────────────┐
    │ CATALOG SERVICE      │
    │ Listens:             │
    │ RatingCreatedEvent   │
    │                      │
    │ Action:              │
    │ 1. Query all ratings │
    │    for productId     │
    │ 2. Recalculate avg   │
    │ 3. Update product    │
    │    averageRating     │
    └────────┬─────────────┘
             │
             ▼
    ┌──────────────────────┐
    │ PUBLISH EVENT:       │
    │ ProductRatingUpdated │
    │ Event                │
    │ {                    │
    │   productId,         │
    │   newAverageRating,  │
    │   totalRatings       │
    │ }                    │
    └──────────────────────┘
```

#### **Sequence for Rating Updates/Deletes:**

```
UpdateRatingEvent / DeleteRatingEvent
    │
    ▼
ReviewService updates/deletes rating
    │
    ▼
Publish: RatingUpdatedEvent / RatingDeletedEvent
    │
    ▼
CatalogService recalculates average
    │
    ▼
Publish: ProductRatingUpdatedEvent
```

---

### 2.4. PRODUCT MANAGEMENT CASCADE (Priority 4 - Medium)
**Type**: Choreography

#### **Product Deletion Flow:**

```
┌─────────────────────────────┐
│ Admin: DELETE /products/id  │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ CATALOG SERVICE             │
│ deleteProduct()             │
│ - Soft delete (status)      │
│ - Save to DB                │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ PUBLISH EVENT:              │
│ ProductDeletedEvent         │
│ {                           │
│   productId,                │
│   productName,              │
│   deletedAt                 │
│ }                           │
└──────────┬──────────────────┘
           │ (RabbitMQ Broadcast)
    ┌──────┴────────────┬──────────────┐
    ▼                   ▼              ▼
CART SERVICE        ORDER SERVICE  REVIEW SERVICE
    │                   │              │
    │ Remove all items  │ Log impact   │ Soft-delete
    │ with productId    │ (don't modify│ reviews for
    │ from all carts    │ existing     │ product
    │                   │ orders)      │
    ▼                   ▼              ▼
Publish             (no event)      (no event)
CartItem
Removed
Event
```

#### **Product Update Flow:**

```
┌──────────────────────────────┐
│ Admin: PUT /products/id      │
│ (name, price, description)   │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ CATALOG SERVICE              │
│ updateProduct()              │
│ - Update fields              │
│ - Save to DB                 │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│ PUBLISH EVENT:               │
│ ProductUpdatedEvent          │
│ {                            │
│   productId,                 │
│   changes: {price, name...}, │
│   updatedAt                  │
│ }                            │
└──────────┬───────────────────┘
           │
    ┌──────┴──────┐
    ▼             ▼
CART SERVICE  ORDER SERVICE
    │             │
    │ Update      │ Update items in
    │ prices in   │ existing orders
    │ user carts  │ (for display)
    │             │
    ▼             ▼
(No event)    (No event)
```

---

### 2.5. ACCOUNT MANAGEMENT CASCADE (Priority 5 - Lower)
**Type**: Choreography

#### **Account Deactivation:**

```
┌────────────────────────────────┐
│ User: PUT /account/deactivate  │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ ACCOUNT SERVICE                │
│ deactivateAccount()            │
│ - Set status = INACTIVE        │
│ - Save to DB                   │
└──────────┬─────────────────────┘
           │
           ▼
┌────────────────────────────────┐
│ PUBLISH EVENT:                 │
│ AccountDeactivatedEvent        │
│ {                              │
│   accountId,                   │
│   deactivatedAt                │
│ }                              │
└──────────┬─────────────────────┘
           │ (RabbitMQ)
    ┌──────┴────────┬──────────────┐
    ▼               ▼              ▼
CART SERVICE    ORDER SERVICE  REVIEW SERVICE
    │               │              │
    │ Archive cart  │ Archive      │ Archive
    │ for account   │ orders for   │ reviews for
    │               │ account      │ account
    ▼               ▼              ▼
(No event)     (No event)     (No event)
```

---

## 3. Event Classes Definition

### Core Events (all need DTO version):

#### Order Domain:
```java
// Event classes should be in separate module or shared package
{
  OrderCreatedEvent(orderId, accountId, totalAmount, items, createdAt),
  OrderCancelledEvent(orderId, reason, cancelledAt),
  OrderConfirmedEvent(orderId, status, confirmedAt)
}

RefundRequestedEvent(refundId, orderId, reason, imageUrls, amount, requestedAt),
RefundApprovedEvent(refundId, orderId, approverName, approvedAt),
RefundCompletedEvent(refundId, transactionId, completedAt),
RefundFailedEvent(refundId, reason, failureCode)
```

#### Payment Domain:
```java
PaymentCreatedEvent(paymentId, orderId, amount, paymentMethod, status),
PaymentCompletedEvent(paymentId, orderId, amount, transactionId),
PaymentFailedEvent(paymentId, orderId, reason, amount)
```

#### Shipping Domain:
```java
ShippingCreatedEvent(shippingId, orderId, address, shippingFee, status),
ShippingStatusChangedEvent(shippingId, orderId, oldStatus, newStatus),
ShippingFailedEvent(shippingId, orderId, reason)
```

#### Cart Domain:
```java
CartClearedEvent(cartId, accountId, clearedAt),
CartItemRemovedEvent(cartId, productId, quantity, removedAt)
```

#### Catalog Domain:
```java
ProductDeletedEvent(productId, productName, deletedAt),
ProductUpdatedEvent(productId, changes, updatedAt),
ProductRatingUpdatedEvent(productId, newAverageRating, totalRatings)
```

#### Review Domain:
```java
RatingCreatedEvent(ratingId, productId, rating, review, createdAt),
RatingUpdatedEvent(ratingId, productId, rating, review, updatedAt),
RatingDeletedEvent(ratingId, productId, deletedAt)
```

#### Account Domain:
```java
AccountDeactivatedEvent(accountId, deactivatedAt)
```

---

## 4. RabbitMQ Configuration Requirements

### Exchanges:
```
- fruitshop.orders (topic) - order-related events
- fruitshop.payments (topic) - payment-related events
- fruitshop.shipping (topic) - shipping-related events
- fruitshop.cart (topic) - cart-related events
- fruitshop.catalog (topic) - product-related events
- fruitshop.review (topic) - review/rating-related events
- fruitshop.account (topic) - account-related events
```

### Queues & Bindings:
```
Queue: order-service-queue
  Bindings:
    - fruitshop.orders.# (all order events)
    - fruitshop.payments.completed (listen for payment completion)
    - fruitshop.payments.failed (listen for payment failures)

Queue: payment-service-queue
  Bindings:
    - fruitshop.orders.created (listen for new orders)
    - fruitshop.refund.approved (listen for approved refunds)

Queue: cart-service-queue
  Bindings:
    - fruitshop.orders.created (listen to clear cart on order)
    - fruitshop.catalog.deleted (listen to remove items when product deleted)

Queue: catalog-service-queue
  Bindings:
    - fruitshop.review.# (all rating events)
    - fruitshop.catalog.# (product updates)

Queue: shipping-service-queue
  Bindings:
    - fruitshop.payments.completed (create shipping after payment)
    - fruitshop.orders.confirmed (prepare shipment)

Queue: review-service-queue
  Bindings:
    - fruitshop.catalog.deleted (soft-delete reviews when product deleted)

Queue: notification-service-queue (if exists)
  Bindings:
    - fruitshop.orders.# (all order events)
    - fruitshop.payments.# (all payment events)
    - fruitshop.refund.# (all refund events)
```

---

## 5. Implementation Phases

### Phase 1: Infrastructure Setup (Week 1-2)
- [ ] Add RabbitMQ to docker-compose.yml
- [ ] Add spring-cloud-stream dependency to all services
- [ ] Create event classes in each service (or shared module)
- [ ] Configure RabbitMQ connection in each service
- [ ] Define topic/queue configuration

### Phase 2: Order Saga - Choreography (Week 2-3)
- [ ] OrderService publishes `OrderCreatedEvent`
- [ ] CartService listens and clears cart
- [ ] PaymentService listens and creates payment
- [ ] ShippingService listens and creates shipping
- [ ] Test happy path & payment failure scenario

### Phase 3: Refund Saga - With Admin Gate (Week 3-4)
- [ ] OrderService publishes `RefundRequestedEvent`
- [ ] Admin approves/rejects via API
- [ ] PaymentService processes approved refunds
- [ ] Retry logic for failed refunds
- [ ] Test all scenarios

### Phase 4: Catalog Cascade (Week 4)
- [ ] CatalogService publishes `ProductDeletedEvent`
- [ ] CartService cleans up items
- [ ] ReviewService soft-deletes reviews
- [ ] CatalogService publishes `ProductUpdatedEvent`
- [ ] CartService & OrderService update prices

### Phase 5: Account Cascade (Week 4-5)
- [ ] AccountService publishes `AccountDeactivatedEvent`
- [ ] Other services archive account data
- [ ] Test data integrity

---

## 6. Error Handling & Resilience

### Dead Letter Queue (DLQ) Strategy:
```
- Every queue has a DLQ for failed messages
- Failed messages stored for 7 days
- Manual retry mechanism via admin dashboard
- Alert monitoring when DLQ receives messages
```

### Idempotency Guards:
```
Each event must include:
- eventId (UUID) - unique per event
- timestamp - for deduplication

Services check if event already processed before applying side effects
Strategy: Store processed eventIds in DB with TTL (7 days)
```

### Circuit Breaker on Event Listeners:
```
If downstream service fails repeatedly:
- Stop consuming from queue temporarily
- Alert admin
- Retry with exponential backoff
- Health check endpoint: GET /health/ready
```

### Message Ordering:
```
RabbitMQ by default: per-partition ordering within queue
For global ordering:
- Order operations by orderId to same partition
- Use routing key: fruitshop.orders.{orderId}
```

---

## 7. Choreography vs Orchestration Decision Table

| Saga | Pattern | Reason | Tradeoff |
|------|---------|--------|----------|
| Order → Payment → Shipping | Choreography | Linear, no loops | Hard to trace end-to-end (use correlation IDs) |
| Refund Approval → Payment | Choreography | Admin gate breaks automated flow | Manual step required anyway |
| Product Delete → Cart cleanup | Choreography | Simple broadcast, parallel consumers | No single point of failure |
| Account Deactivate → Archive | Choreography | Fire-and-forget archival | No guarantee all services heard |

**Note**: Could add Orchestration Saga pattern later with Temporal.io or dedicated Saga Orchestrator if business rules get complex (e.g., conditional routing, timeout handling).

---

## 8. Testing Strategy

### Unit Tests:
- Test each event publisher in isolation
- Mock RabbitMQ, verify event shape

### Integration Tests:
- Spin up embedded RabbitMQ
- Test full saga: Order → Payment → Shipping
- Verify state changes in each service

### E2E Tests:
- Full docker-compose with all services
- Simulate scenarios: success, payment failure, shipping delay
- Verify compensations trigger correctly

---

## 9. Monitoring & Observability

### Required Metrics:
```
- Event publish rate (per service, per event type)
- Event consume rate (per service, per binding)
- Consumer lag (how far behind real-time)
- DLQ depth (# failed messages)
- End-to-end latency (Order creation → Order ready time)
- Compensation trigger rate (failures requiring refund)
```

### Correlation IDs:
```
Each saga transaction needs:
- sagaId (UUID generated at Order creation)
- Include in all events and logs
- Enables tracing: Order → Payment → Shipping → Confirmation
```

---

## 10. RabbitMQ Deployment Notes

```yaml
# docker-compose.yml addition
rabbitmq:
  image: rabbitmq:3.13-management-alpine
  ports:
    - "5672:5672"      # AMQP
    - "15672:15672"    # Management UI
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  volumes:
    - rabbitmq_data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

---

## Summary

This Saga pattern architecture uses **Choreography** for event-driven simplicity with **RabbitMQ** as the async transport layer. Key principles:

1. **Events are facts** - published after state change is persisted
2. **No sync REST for writes** - use events for side effects
3. **Eventual consistency** - services may lag, consistency within acceptable bounds
4. **Compensation triggers** - failures publish events that trigger refunds/cancellations
5. **Idempotency required** - all event handlers must be safe to retry

Next steps: Implement infrastructure (RabbitMQ) then Phase 2 (Order Saga).
