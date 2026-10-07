package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Flow 6: Real-time AI Assistant Chatbot
 *
 * Business Context:
 *   Simulates customers interacting with the AI chatbot sales assistant
 *   powered by Groq AI API. This flow is the most latency-sensitive due to
 *   external AI API calls that can block worker threads if not handled async.
 *
 *   Key concerns under load:
 *     - External Groq API latency (network I/O bound)
 *     - Thread exhaustion if AI calls block servlet threads
 *     - Session state management under concurrent chat users
 *
 *   Represents ~5% of total traffic.
 *
 * SLA Targets:
 *   - p95 Response Time for AI message: < 1500ms (Groq API latency)
 *   - p95 Response Time for session ops: < 500ms
 *   - Error Rate: < 1.0%
 *
 * Load Profile (5% of 100 base users = 5 users):
 *   - Ramp 5 users over 10s → steady 2 users/sec for 30s
 *
 * Per-User Isolation:
 *   Each virtual user is assigned its own accountId and sessionId
 *   via a circular feeder (SLOT_COUNT = 20).
 *   Slot n → accountId = acc-n, sessionId = sess-n
 *
 * Run:
 *   mvn gatling:test -Dgatling.simulationClass=server.FruitShop.gatling.Flow6AIChatbotSimulation
 */
public class Flow6AIChatbotSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol Configuration
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Flow6-AIChatbot/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Per-User Data Feeder
    //    Each slot has its own accountId and sessionId so that
    //    no two virtual users share the same chat session.
    //
    //    Slot n → accountId = acc-n
    //              sessionId = sess-n
    //
    //    The feeder is circular so it works even when more than
    //    SLOT_COUNT virtual users are injected.
    // ─────────────────────────────────────────────────────────
    private static final int SLOT_COUNT = 20;

    private final Iterator<Map<String, Object>> chatFeeder =
            Stream.iterate(1, n -> (n % SLOT_COUNT) + 1)   // 1, 2, …, 20, 1, 2, …
                    .map(n -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("accountId", "acc-"  + n);
                        data.put("sessionId", "sess-" + n);
                        return data;
                    })
                    .iterator();

    // ─────────────────────────────────────────────────────────
    // 3. Scenario: AI Assistant Chatbot Conversation
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder flow6AIChatbot = scenario("Flow 6: Real-time AI Assistant Chatbot")
            .feed(chatFeeder)

            // Step 1: Create a new chat session for the user
            .exec(http("POST Create Chat Session")
                    .post("/api/chat/sessions")
                    .body(StringBody(
                            "{\"accountId\":\"#{accountId}\"," +
                            "\"title\":\"Tư vấn hoa quả\"}"
                    ))
                    .check(status().in(200, 201, 400)))
            .pause(Duration.ofSeconds(1))

            // Step 2: Load existing sessions for the user (sidebar rendering)
            .exec(http("GET Chat Sessions by Account")
                    .get("/api/chat/sessions/account/#{accountId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 3: Send first message to AI chatbot (product advice intent)
            //         This triggers an external Groq AI API call – high latency expected
            .exec(http("POST Send AI Chat Message (Product Advice)")
                    .post("/api/chat/messages")
                    .body(StringBody(
                            "{" +
                            "\"sessionId\":\"#{sessionId}\"," +
                            "\"senderId\":\"#{accountId}\"," +
                            "\"content\":\"Tư vấn giúp tôi loại hoa quả bổ dưỡng nhất\"," +
                            "\"intent\":\"PRODUCT_ADVICE\"" +
                            "}"
                    ))
                    .check(status().in(200, 201, 500)))   // 500 if Groq API is down
            .pause(Duration.ofSeconds(2), Duration.ofSeconds(5))   // user reads AI response

            // Step 4: Send a follow-up message (price inquiry intent)
            .exec(http("POST Send AI Chat Message (Price Inquiry)")
                    .post("/api/chat/messages")
                    .body(StringBody(
                            "{" +
                            "\"sessionId\":\"#{sessionId}\"," +
                            "\"senderId\":\"#{accountId}\"," +
                            "\"content\":\"Giá mỗi kg xoài cát Hòa Lộc là bao nhiêu?\"," +
                            "\"intent\":\"PRICE_INQUIRY\"" +
                            "}"
                    ))
                    .check(status().in(200, 201, 500)))
            .pause(Duration.ofSeconds(2), Duration.ofSeconds(4))

            // Step 5: Retrieve full message history for the session
            .exec(http("GET Chat Message History")
                    .get("/api/chat/messages/#{sessionId}")
                    .check(status().in(200, 404)))
            .pause(Duration.ofMillis(500))

            // Step 6: Mark all messages in session as read
            .exec(http("PATCH Mark Messages as Read")
                    .patch("/api/chat/sessions/#{sessionId}/read")
                    .check(status().in(200, 204, 404)))
            .pause(Duration.ofMillis(500))

            // Step 7: Get full session details (including messages)
            .exec(http("GET Chat Session Detail")
                    .get("/api/chat/sessions/#{sessionId}")
                    .check(status().in(200, 404)));

    // ─────────────────────────────────────────────────────────
    // 4. Load Injection Profile
    //    5% weight → ramp 5 users / steady 2 users/sec
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                flow6AIChatbot.injectOpen(
                        rampUsers(5).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(2).during(Duration.ofSeconds(30))
                )
        ).protocols(httpProtocol)
         .assertions(
                 global().responseTime().percentile3().lte(1500),  // p95 < 1500ms (AI latency)
                 global().failedRequests().percent().lte(1.0)       // error rate < 1%
         );
    }
}
