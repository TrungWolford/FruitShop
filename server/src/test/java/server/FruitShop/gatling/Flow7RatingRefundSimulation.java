package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 7: Post-Purchase Rating & Refund Request
 *
 * Business Context:
 *   Simulates customers performing post-purchase actions:
 *     1. Rating a product after delivery confirmation
 *     2. Reading aggregated product reviews
 *     3. Submitting a refund request if goods are damaged/wrong
 *     4. Tracking refund status by order ID
 *
 *   Although low traffic (~5%), refund processing is business-critical:
 *   errors here directly impact customer trust and retention.
 *   Represents ~5% of total traffic.
 *
 * SLA Targets:
 *   - p95 Response Time for POST (rating/refund): < 500ms
 *   - p95 Response Time for GET (read ops): < 200ms
 *   - Error Rate: < 1.0%
 *
 * Load Profile (5% of 100 base users = 5 users):
 *   - Ramp 5 users over 10s → steady 2 users/sec for 30s
 *
 * Per-User Isolation:
 *   Each virtual user is assigned its own accountId, orderId and productId
 *   via a circular feeder (SLOT_COUNT = 20).
 *   Slot n → accountId = acc-n, orderId = ord-n, productId = p-n
 *
 * Run:
 *   mvn gatling:test -Dgatling.simulationClass=server.FruitShop.gatling.Flow7RatingRefundSimulation
 */
public class Flow7RatingRefundSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow7-RatingRefund/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Per-User Data Feeder
    //    Each slot has its own accountId, orderId and productId
    //    so that no two virtual users share the same rating/refund data.
    //
    //    Slot n → accountId = acc-n
    //              orderId   = ord-n
    //              productId = p-n
    //
    //    The feeder is circular so it works even when more than
    //    SLOT_COUNT virtual users are injected.
    // ─────────────────────────────────────────────────────────
    private static final int SLOT_COUNT = 20;

    private final Iterator<Map<String, Object>> ratingRefundFeeder =
            Stream.iterate(1, n -> (n % SLOT_COUNT) + 1)   // 1, 2, …, 20, 1, 2, …
                    .map(n -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("accountId", "acc-" + n);
                        data.put("orderId",   "ord-" + n);
                        data.put("productId", "p-"   + n);
                        return data;
                    })
                    .iterator();

    // ─────────────────────────────────────────────────────────
    // 3. Scenario: Post-Purchase Rating & Refund Request
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow7RatingAndRefund = scenario("Flow 7: Post-Purchase Rating & Refund Request")
            .feed(ratingRefundFeeder)

            // Step 1: Read existing product ratings before submitting own review
            .exec(http("GET Product Ratings List")
                    .get("/api/rating/product/#{productId}?page=0&size=10")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 2: Read average rating score (shown on product page)
            .exec(http("GET Product Average Rating Score")
                    .get("/api/rating/product/#{productId}/average")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 3: Submit a new product rating (5-star with comment)
            .exec(http("POST Create Product Rating")
                    .post("/api/rating")
                    .body(StringBody(
                            "{" +
                            "\"accountId\":\"#{accountId}\"," +
                            "\"productId\":\"#{productId}\"," +
                            "\"star\":5," +
                            "\"comment\":\"Trái cây rất tươi ngon, đóng gói kỹ càng, giao nhanh!\"" +
                            "}"
                    ))
                    .check(status().in(200, 201, 400)))   // 400 if already rated this product
            .pause(Duration.ofSeconds(1))

            // Step 4: View this account's own ratings (profile review history)
            .exec(http("GET Ratings by Account")
                    .get("/api/rating/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 5: Look up the completed order to initiate refund
            .exec(http("GET Order History for Refund Lookup")
                    .get("/api/order/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 6: Submit a refund request for a damaged/wrong order
            .exec(http("POST Create Refund Request")
                    .post("/api/refund")
                    .body(StringBody(
                            "{" +
                            "\"orderId\":\"#{orderId}\"," +
                            "\"reason\":\"Hàng bị dập, không đúng loại đã đặt\"," +
                            "\"amount\":50000" +
                            "}"
                    ))
                    .check(status().in(200, 201, 400)))   // 400 if refund already requested
            .pause(Duration.ofSeconds(1))

            // Step 7: Check refund request status by order ID
            .exec(http("GET Refund Request by Order ID")
                    .get("/api/refund/order/#{orderId}")
                    .check(status().in(200, 404)));

    // ─────────────────────────────────────────────────────────
    // 4. Load Injection Profile
    //    5% weight → ramp 5 users / steady 2 users/sec
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow7RatingAndRefund.injectOpen(
                        rampUsers(5).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(2).during(Duration.ofSeconds(30))
                )
        ).protocols(httpProtocol)
         .assertions(
                 global().responseTime().percentile3().lte(500),   // p95 < 500ms
                 global().failedRequests().percent().lte(1.0)       // error rate < 1%
         );
    }
}
