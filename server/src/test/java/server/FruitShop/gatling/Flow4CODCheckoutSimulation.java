package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 4: Standard COD Checkout Flow (Cash on Delivery)
 *
 * Business Context:
 *   Simulates the most common end-to-end checkout journey using COD payment.
 *   This is the critical revenue-generating path that involves:
 *     - DB transaction with inventory decrement
 *     - Cart-to-order state transfer
 *     - Post-order order history retrieval
 *   Represents ~10% of total traffic but is the highest business-critical flow.
 *
 *   Steps: View cart → Get shipping address → Create payment record → Create order →
 *          Clear cart → View order history → View order status
 *
 * SLA Targets:
 *   - p95 Response Time for POST /api/order: < 500ms
 *   - p95 Response Time for GET endpoints: < 200ms
 *   - Error Rate: < 1.0%
 *
 * Load Profile (10% of 100 base users = 10 users):
 *   - Ramp 20 users over 10s → steady 14 users/sec for 20s
 *
 * Per-User Isolation:
 *   Each virtual user is assigned its own accountId, shippingId, orderId and
 *   productId via a circular feeder (SLOT_COUNT = 20).
 *   Slot n → accountId = acc-n, shippingId = ship-n, orderId = ord-n, productId = p-n
 *
 * Run:
 *   mvn gatling:test "-Dgatling.simulationClass=server.FruitShop.gatling.Flow4CODCheckoutSimulation"
 */
public class Flow4CODCheckoutSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow4-CODCheckout/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Per-User Data Feeder
    //    Each slot has its own accountId, shippingId, orderId, productId
    //    so that no two virtual users share the same checkout data.
    //
    //    Slot n → accountId  = acc-n
    //              shippingId = ship-n
    //              orderId    = ord-n
    //              productId  = p-n
    //
    //    The feeder is circular so it works even when more than
    //    SLOT_COUNT virtual users are injected.
    // ─────────────────────────────────────────────────────────
    private static final int SLOT_COUNT = 20;

    private final Iterator<Map<String, Object>> checkoutFeeder =
            Stream.iterate(1, n -> (n % SLOT_COUNT) + 1)   // 1, 2, …, 20, 1, 2, …
                    .map(n -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("accountId",   "acc-"  + n);
                        data.put("shippingId",  "ship-" + n);
                        data.put("orderId",     "ord-"  + n);
                        data.put("productId",   "p-"    + n);
                        return data;
                    })
                    .iterator();

    // ─────────────────────────────────────────────────────────
    // 3. Scenario: Standard COD Checkout & Order History
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow4CODCheckout = scenario("Flow 4: Standard COD Checkout & Order History")
            .feed(checkoutFeeder)

            // Step 1: View cart items before proceeding to checkout
            .exec(http("GET Cart Items Before Checkout")
                    .get("/api/cart/account/#{accountId}/items")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 2: Load shipping addresses to choose delivery address
            .exec(http("GET Shipping Addresses for Checkout")
                    .get("/api/shipping/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 3: Create COD payment record (internal payment entry)
            .exec(http("POST Create Payment Record (COD)")
                    .post("/api/payment")
                    .body(StringBody(
                            "{\"paymentMethod\":\"COD\"," +
                            "\"paymentStatus\":0," +
                            "\"amount\":150000}"
                    ))
                    .check(status().in(200, 201, 400)))
            .pause(Duration.ofSeconds(1))

            // Step 4: Create the order – DB transaction, inventory decrement (CRITICAL)
            .exec(http("POST Create Order (COD)")
                    .post("/api/order")
                    .body(StringBody(
                            "{" +
                            "\"accountId\":\"#{accountId}\"," +
                            "\"shippingId\":\"#{shippingId}\"," +
                            "\"paymentMethod\":0," +
                            "\"totalPrice\":150000," +
                            "\"items\":[{" +
                            "\"productId\":\"#{productId}\"," +
                            "\"unitPrice\":75000," +
                            "\"quantity\":2," +
                            "\"totalPrice\":150000" +
                            "}]" +
                            "}"
                    ))
                    .check(status().in(200, 201, 400)))
            .pause(Duration.ofSeconds(1))

            // Step 5: Clear the cart after successful order placement
            .exec(http("DELETE Clear Cart After Checkout")
                    .delete("/api/cart/account/#{accountId}/clear")
                    .check(status().in(200, 204, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 6: Retrieve order history (user checks order was created)
            .exec(http("GET Order History by Account")
                    .get("/api/order/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 7: Filter orders by PENDING status
            .exec(http("GET Orders by Status PENDING")
                    .get("/api/order/status/PENDING?page=0&size=5")
                    .check(status().in(200, 400)));

    // ─────────────────────────────────────────────────────────
    // 4. Load Injection Profile
    //    10% weight → ramp 20 users / steady 14 users/sec
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow4CODCheckout.injectOpen(
                        // Giai đoạn 1: Tăng dần 20 users trong 10 giây
                        rampUsers(20).during(Duration.ofSeconds(10)),
                        // Giai đoạn 2: Bơm đều 14 users/giây trong 20 giây (14 * 20 = 280 users)
                        constantUsersPerSec(14).during(Duration.ofSeconds(20))
                )
        ).protocols(httpProtocol);
    }
}
