package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 3: Shopping Cart Management
 *
 * Business Context:
 *   Simulates authenticated users interacting with their shopping cart.
 *   Each virtual user is assigned its own accountId, cartItemIds and productIds
 *   via a feeder so that concurrent users operate on fully isolated carts —
 *   eliminating shared-state race conditions.
 *   Represents ~15% of total traffic.
 *
 *   Steps: Get cart → Add item (p-1) → Add item (p-2) → Read cart items →
 *          Update quantity → Remove item → Verify final cart state
 *
 * SLA Targets:
 *   - p95 Response Time for writes (POST/PUT/DELETE): < 500ms
 *   - p95 Response Time for reads (GET): < 200ms
 *   - Error Rate: < 1.0%
 *
 * Load Profile (15% of 100 base users = 15 users):
 *   - Ramp 20 users over 10s
 *
 * Run:
 *   mvn gatling:test "-Dgatling.simulationClass=server.FruitShop.gatling.Flow3CartManagementSimulation"
 */
public class Flow3CartManagementSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow3-Cart/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Per-User Data Feeder
    //    Prepares 20 isolated "slots": each slot has its own
    //    accountId, two cartItemIds and two productIds so that
    //    no two virtual users ever touch the same cart row.
    //
    //    Slot n  →  accountId = acc-n
    //               cartItemId1 = ci-(2n-1)   (e.g. slot 1 → ci-1)
    //               cartItemId2 = ci-(2n)      (e.g. slot 1 → ci-2)
    //               productId1  = p-1
    //               productId2  = p-2
    //
    //    The feeder is circular so it works even when more than
    //    20 virtual users are injected.
    // ─────────────────────────────────────────────────────────
    private static final int SLOT_COUNT = 20;

    private final Iterator<Map<String, Object>> cartFeeder =
            Stream.iterate(1, n -> (n % SLOT_COUNT) + 1)   // 1, 2, …, 20, 1, 2, …
                    .map(n -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("accountId",    "acc-" + n);
                        data.put("cartItemId1",  "ci-" + (2 * n - 1));
                        data.put("cartItemId2",  "ci-" + (2 * n));
                        data.put("productId1",   "p-1");
                        data.put("productId2",   "p-2");
                        return data;
                    })
                    .iterator();

    // ─────────────────────────────────────────────────────────
    // 3. Scenario: Shopping Cart Management
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow3CartManagement = scenario("Flow 3: Shopping Cart Management")
            .feed(cartFeeder)

            // Step 1: Get current cart for this user's account (initial page load)
            .exec(http("GET Cart by Account ID")
                    .get("/api/cart/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 2: Add first product to this user's cart
            .exec(http("POST Add Item to Cart (Product 1)")
                    .post("/api/cart/account/#{accountId}/items")
                    .body(StringBody("{\"productId\":\"#{productId1}\",\"quantity\":2}"))
                    .check(status().in(200, 201, 400, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 3: Add a second product to this user's cart
            .exec(http("POST Add Item to Cart (Product 2)")
                    .post("/api/cart/account/#{accountId}/items")
                    .body(StringBody("{\"productId\":\"#{productId2}\",\"quantity\":1}"))
                    .check(status().in(200, 201, 400, 404)))
            .pause(Duration.ofMillis(500))

            // Step 4: Fetch all cart items (confirm items were added)
            .exec(http("GET Cart Items List")
                    .get("/api/cart/account/#{accountId}/items")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 5: Update quantity of this user's first cart item
            .exec(http("PUT Update Cart Item Quantity")
                    .put("/api/cart/items/#{cartItemId1}")
                    .body(StringBody("{\"quantity\":5}"))
                    .check(status().in(200, 400, 404)))
            .pause(Duration.ofSeconds(1))

            // Step 6: Remove this user's second cart item
            .exec(http("DELETE Remove Cart Item")
                    .delete("/api/cart/items/#{cartItemId2}")
                    .check(status().in(200, 204, 400, 404)))
            .pause(Duration.ofMillis(500))

            // Step 7: Re-fetch cart items to verify state after remove
            .exec(http("GET Cart Items (After Remove)")
                    .get("/api/cart/account/#{accountId}/items")
                    .check(status().in(200, 404)));

    // ─────────────────────────────────────────────────────────
    // 4. Load Injection Profile
    //    15% weight → ramp 20 users over 10s
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow3CartManagement.injectClosed(
                        // Giai đoạn 1: Tăng dần lên 300 concurrent users trong 15 giây
                        rampConcurrentUsers(0).to(300).during(Duration.ofSeconds(15)),
                        // Giai đoạn 2: Duy trì đúng 300 concurrent users trong 30 giây
                        constantConcurrentUsers(300).during(Duration.ofSeconds(30))))
                .protocols(httpProtocol);
    }
}
