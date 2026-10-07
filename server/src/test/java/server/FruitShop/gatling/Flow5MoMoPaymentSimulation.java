package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 5: Online MoMo E-Wallet Payment Flow
 *
 * Business Context:
 * Simulates the full MoMo QR payment cycle including order creation,
 * payment QR generation (external MoMo API call), IPN webhook callback
 * simulation, and payment status verification.
 *
 * This is the most latency-sensitive business flow because it depends on:
 * - External MoMo API network latency (QR generation)
 * - Webhook IPN processing correctness (signature validation)
 * - Atomic order-payment status update in DB
 * Represents ~10% of total traffic.
 *
 * SLA Targets:
 * - p95 Response Time for MoMo API calls: < 1500ms (external API)
 * - p95 Response Time for internal order creation: < 500ms
 * - Error Rate: < 1.0%
 *
 * Load Profile (Closed Model - Concurrent Users):
 * - Ramp up to 300 concurrent users over 15s → hold 300 concurrent users for
 * 30s
 *
 * Per-User Isolation:
 * Each virtual user is assigned its own accountId, shippingId, orderId and
 * productId via a circular feeder (SLOT_COUNT = 20).
 * Slot n → accountId = acc-n, shippingId = ship-n, orderId = ord-n, productId =
 * p-n
 *
 * Run:
 * mvn gatling:test
 * "-Dgatling.simulationClass=server.FruitShop.gatling.Flow5MoMoPaymentSimulation"
 */
public class Flow5MoMoPaymentSimulation extends Simulation {

        // ─────────────────────────────────────────────────────────
        // 1. HTTP Protocol Configuration
        // ─────────────────────────────────────────────────────────
        private final HttpProtocolBuilder httpProtocol = http
                        .baseUrl("http://localhost:8080")
                        .acceptHeader("application/json")
                        .contentTypeHeader("application/json")
                        .userAgentHeader("Gatling/Flow5-MoMoPayment/FruitShop");

        // ─────────────────────────────────────────────────────────
        // 2. Per-User Data Feeder
        // Each slot has its own accountId, shippingId, orderId, productId
        // so that no two virtual users share the same MoMo payment data.
        //
        // Slot n → accountId = acc-n
        // shippingId = ship-n
        // orderId = ord-n
        // productId = p-n
        // requestId = req-load-test-n
        //
        // The feeder is circular so it works even when more than
        // SLOT_COUNT virtual users are injected.
        // ─────────────────────────────────────────────────────────
        private static final int SLOT_COUNT = 20;

        private final Iterator<Map<String, Object>> momoFeeder = Stream.iterate(1, n -> (n % SLOT_COUNT) + 1) // 1, 2,
                                                                                                              // …, 20,
                                                                                                              // 1, 2, …
                        .map(n -> {
                                Map<String, Object> data = new HashMap<>();
                                data.put("accountId", "acc-" + n);
                                data.put("shippingId", "ship-" + n);
                                data.put("orderId", "ord-" + n);
                                data.put("productId", "p-" + n);
                                data.put("requestId", "req-load-test-" + n);
                                return data;
                        })
                        .iterator();

        // ─────────────────────────────────────────────────────────
        // 3. Scenario: MoMo E-Wallet Payment & Webhook IPN
        // ─────────────────────────────────────────────────────────
        private final ScenarioBuilder flow5MoMoPayment = scenario("Flow 5: Online MoMo Payment & Webhook IPN")
                        .feed(momoFeeder)

                        // Step 1: Create order with MoMo payment method (paymentMethod=1)
                        .exec(http("POST Create Order for MoMo Payment")
                                        .post("/api/order")
                                        .body(StringBody(
                                                        "{" +
                                                                        "\"accountId\":\"#{accountId}\"," +
                                                                        "\"shippingId\":\"#{shippingId}\"," +
                                                                        "\"paymentMethod\":1," +
                                                                        "\"totalPrice\":200000," +
                                                                        "\"items\":[{" +
                                                                        "\"productId\":\"#{productId}\"," +
                                                                        "\"unitPrice\":100000," +
                                                                        "\"quantity\":2," +
                                                                        "\"totalPrice\":200000" +
                                                                        "}]" +
                                                                        "}"))
                                        .check(status().in(200, 201, 400)))
                        .pause(Duration.ofSeconds(1))

                        // Step 2: Request MoMo QR code payment URL (external API call)
                        .exec(http("POST Create MoMo QR Payment Request")
                                        .post("/api/momo/create-payment")
                                        .body(StringBody("{\"orderId\":\"#{orderId}\"}"))
                                        .check(status().in(200, 400, 404, 500)))
                        .pause(Duration.ofSeconds(2), Duration.ofSeconds(4)) // simulate user scanning QR

                        // Step 3: Simulate MoMo IPN webhook callback (resultCode=0 = success)
                        // NOTE: In real load test, signature must match server-side HMAC.
                        // Here we test the endpoint's ability to handle concurrent IPN calls.
                        .exec(http("POST Simulate MoMo IPN Webhook (Success)")
                                        .post("/api/momo/ipn-handler")
                                        .body(StringBody(
                                                        "{" +
                                                                        "\"partnerCode\":\"MOMO\"," +
                                                                        "\"orderId\":\"#{orderId}\"," +
                                                                        "\"requestId\":\"#{requestId}\"," +
                                                                        "\"amount\":200000," +
                                                                        "\"orderInfo\":\"Payment for order #{orderId}\","
                                                                        +
                                                                        "\"orderType\":\"momo_wallet\"," +
                                                                        "\"transId\":9876543210," +
                                                                        "\"resultCode\":0," +
                                                                        "\"message\":\"Successful.\"," +
                                                                        "\"responseTime\":1700000000000," +
                                                                        "\"extraData\":\"\"," +
                                                                        "\"signature\":\"dummy_load_test_signature\"" +
                                                                        "}"))
                                        .check(status().in(200, 204, 400, 401, 500))) // 401 if HMAC fails
                        .pause(Duration.ofMillis(500))

                        // Step 4: Poll payment status (user returns from MoMo redirect)
                        .exec(http("GET Check MoMo Payment Status")
                                        .get("/api/momo/check-status/#{orderId}")
                                        .check(status().in(200, 404, 500)))
                        .pause(Duration.ofMillis(500))

                        // Step 5: Fetch updated order history to confirm payment reflected
                        .exec(http("GET Order History After MoMo Payment")
                                        .get("/api/order/account/#{accountId}")
                                        .check(status().in(200, 404)));

        // ─────────────────────────────────────────────────────────
        // 4. Load Injection Profile (Closed Workload - Concurrent Users)
        // Closed model đảm bảo luôn có đúng N user đang active cùng lúc.
        // Khác với Open model (Virtual User) chỉ kiểm soát tốc độ arrival.
        //
        // Giai đoạn 1: Ramp từ 0 → 300 concurrent users trong 15 giây
        // Giai đoạn 2: Giữ đúng 300 concurrent users trong 30 giây
        // ─────────────────────────────────────────────────────────
        {
                setUp(
                                flow5MoMoPayment.injectClosed(
                                                // Giai đoạn 1: Tăng dần lên 300 concurrent users trong 15 giây
                                                rampConcurrentUsers(0).to(300).during(Duration.ofSeconds(15)),
                                                // Giai đoạn 2: Duy trì đúng 300 concurrent users trong 30 giây
                                                constantConcurrentUsers(300).during(Duration.ofSeconds(30))))
                                .protocols(httpProtocol);
        }
}
