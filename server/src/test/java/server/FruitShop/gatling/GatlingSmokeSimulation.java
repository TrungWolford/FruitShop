package server.FruitShop.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Smoke Test Simulation for FruitShop Backend API.
 *
 * Goal : Verify that the backend is reachable and responding correctly.
 * Load : 1 virtual user, runs once, no ramp-up.
 * Pass : GET /api/product/top-10 returns HTTP 200.
 *
 * Run:
 * mvn gatling:test -Dgatling.simulationClass=simulations.GatlingSmokeSimulation
 */
public class GatlingSmokeSimulation extends Simulation {

    // ─────────────────────────────────────────────────────────
    // 1. HTTP Protocol
    // ─────────────────────────────────────────────────────────
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://fruitshop-c4.vercel.app/")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/SmokeTest/FruitShop");

    // ─────────────────────────────────────────────────────────
    // 2. Scenario: 1 request, expect 200
    // ─────────────────────────────────────────────────────────
    private final ScenarioBuilder smokeScenario = scenario("Smoke Test: GET /api/product/top-10")
            .exec(
                    http("GET Top 10 Products")
                            .get("/api/product/top-10")
                            .check(status().is(200)));

    // ─────────────────────────────────────────────────────────
    // 3. Load Injection: exactly 1 virtual user, runs once
    // ─────────────────────────────────────────────────────────
    {
        setUp(
                smokeScenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
