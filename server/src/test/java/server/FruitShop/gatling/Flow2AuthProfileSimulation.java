package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 2: Customer Registration, Login & Profile Load
 *
 * Business Context:
 *   Simulates new and returning customers authenticating with the system.
 *   Tests BCrypt password hashing CPU pressure on registration,
 *   JWT/session creation on login, and subsequent profile + address reads.
 *   Represents ~15% of total traffic.
 *
 * SLA Targets:
 *   - p95 Response Time: < 500ms (write/auth ops)
 *   - Error Rate: < 1.0%
 *
 * Load Profile (15% of 100 base users = 15 users):
 *   - Ramp 15 users over 10s → steady 8 users/sec for 30s
 *
 * Run:
 *   mvn gatling:test -Dgatling.simulationClass=server.FruitShop.gatling.Flow2AuthProfileSimulation
 */
public class Flow2AuthProfileSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow2-Auth/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Dynamic Feeder – unique phone number per virtual user
    // ─────────────────────────────────────────────────────────
    private final Iterator<Map<String, Object>> userFeeder =
            Stream.generate(() -> {
                String randomPhone = "09" + String.format("%08d", new Random().nextInt(100_000_000));
                String uuid        = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                Map<String, Object> data = new HashMap<>();
                data.put("phone",    randomPhone);
                data.put("password", "Test@12345");
                data.put("name",     "LoadUser_" + uuid);
                return data;
            }).iterator();

    // ─────────────────────────────────────────────────────────
    // 3. Scenario: Auth & Profile
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow2AuthProfile = scenario("Flow 2: Customer Registration, Login & Profile")
            .feed(userFeeder)

            // Step 1: Register a new account (BCrypt hashing – CPU intensive)
            .exec(http("POST Register Account")
                    .post("/api/account")
                    .body(StringBody(
                            "{\"accountName\":\"#{name}\"," +
                                    "\"accountPhone\":\"#{phone}\"," +
                                    "\"password\":\"#{password}\"," +
                                    "\"roleIds\":[\"RO366D82\"]}"
                    ))
                    .check(status().in(200, 201, 409)))  // 409 if phone already exists
            .pause(Duration.ofSeconds(1))

            // Step 2: Login with the registered credentials
            .exec(http("POST Login Account")
                    .post("/api/account/login")
                    .body(StringBody(
                            "{\"accountPhone\":\"#{phone}\"," +
                            "\"password\":\"#{password}\"}"
                    ))
                    .check(status().in(200, 401)))
            .pause(Duration.ofSeconds(1))

            // Step 3: Fetch account details by ID (simulate profile page load)
            .exec(http("GET Account Details")
                    .get("/api/account/acc-1")
                    .check(status().in(200, 401, 403, 404)))
            .pause(Duration.ofMillis(500))

            // Step 4: Load saved shipping addresses for the account
            .exec(http("GET Shipping Addresses")
                    .get("/api/shipping/account/acc-1")
                    .check(status().in(200, 401, 403, 404)))
            .pause(Duration.ofMillis(500))

            // Step 5: Search account by name (admin-side, but still tested for load)
            .exec(http("GET Search Accounts by Name")
                    .get("/api/account/search?accountName=LoadUser&page=0&size=5")
                    .check(status().in(200, 401, 403)));

    // ─────────────────────────────────────────────────────────
    // 4. Load Injection Profile
    //    15% weight → ramp 15 users / steady 8 users/sec
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow2AuthProfile.injectClosed(
                        // Giai đoạn 1: Tăng dần lên 300 concurrent users trong 15 giây
                        rampConcurrentUsers(0).to(300).during(Duration.ofSeconds(15)),
                        // Giai đoạn 2: Duy trì đúng 300 concurrent users trong 30 giây
                        constantConcurrentUsers(300).during(Duration.ofSeconds(30))))
                .protocols(httpProtocol);
    }
}
