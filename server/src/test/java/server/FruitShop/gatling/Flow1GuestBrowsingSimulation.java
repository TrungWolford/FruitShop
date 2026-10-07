package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 1: Guest Browsing & Product Discovery
 *
 * Business Context:
 *   Simulates anonymous users (non-logged-in) browsing the fruit shop.
 *   This is the highest-traffic flow, representing ~40% of total traffic.
 *   Covers: homepage top-products, category listing, product search,
 *           price filtering, product detail page, and product ratings.
 *
 * Load Profile (40% of 100 base users = 40 users):
 *   - Ramp 40 users over 10s → steady 20 users/sec for 30s
 *
 * Run:
 *   mvn gatling:test "-Dgatling.simulationClass=server.FruitShop.gatling.Flow1GuestBrowsingSimulation"
 */
public class Flow1GuestBrowsingSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow1-GuestBrowsing/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Scenario: Guest Browsing & Product Discovery
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow1GuestBrowsing = scenario("Flow 1: Guest Browsing & Product Discovery")

            // Step 1: Visit homepage – fetch top-selling products (high frequency)
            .exec(http("GET Top 10 Products")
                    .get("/api/product/top-10")
                    .check(status().is(200)))
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(2))

            // Step 2: Load category list to browse by category
            .exec(http("GET All Categories")
                    .get("/api/category")
                    .check(status().is(200)))
            .pause(Duration.ofSeconds(1))

            // Step 3: Search products by keyword (most CPU/DB-intensive read path)
            .exec(http("Search Products by Keyword")
                    .get("/api/product/search?keywords=T%C3%A1o&page=0&size=10")
                    .check(status().is(200)))
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))

            // Step 4: Filter products by price range and category
            .exec(http("Filter Products by Price Range")
                    .get("/api/product/filter?minPrice=20000&maxPrice=500000&page=0&size=10")
                    .check(status().is(200)))
            .pause(Duration.ofSeconds(1))

            // Step 5: View all products (paginated listing)
            .exec(http("GET All Products (Paginated)")
                    .get("/api/product?page=0&size=12")
                    .check(status().is(200)))
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(2))

            // Step 6: Click into a product detail page
            .exec(http("GET Product Detail")
                    .get("/api/product/p-1")
                    .check(status().in(200, 404)))
            .pause(Duration.ofSeconds(2), Duration.ofSeconds(4))

            // Step 7: Read product ratings while viewing detail
            .exec(http("GET Product Ratings List")
                    .get("/api/rating/product/p-1?page=0&size=5")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 8: Read average rating score for the product
            .exec(http("GET Product Average Rating")
                    .get("/api/rating/product/p-1/average")
                    .check(status().in(200, 404)));

    // ─────────────────────────────────────────────────────────
    // 3. Load Injection Profile
    //    40% weight → ramp 40 users / steady 20 users/sec
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow1GuestBrowsing.injectOpen(
                        // Giai đoạn 1: Tăng dần 20 users trong 10 giây
                        rampUsers(20).during(Duration.ofSeconds(10)),
                        // Giai đoạn 2: Bơm đều 14 users/giây trong 20 giây (14 * 20 = 280 users)
                        constantUsersPerSec(14).during(Duration.ofSeconds(20))
                )
        ).protocols(httpProtocol);
    }
}
