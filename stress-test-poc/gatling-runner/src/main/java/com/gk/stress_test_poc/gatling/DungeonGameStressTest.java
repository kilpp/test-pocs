package com.gk.stress_test_poc.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class DungeonGameStressTest extends Simulation {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Base configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS = 50;
    private static final int RAMP_DURATION = 30;
    private static final int TEST_DURATION = 120;

    // HTTP protocol configuration
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/DungeonGame-LoadTest");

    // Feeder
    private final Iterator<Map<String, Object>> gameDataFeeder =
            Stream.generate(this::generateGameData)
                    .iterator();

    private Map<String, Object> generateGameData() {
        var gameRequest = new DungeonGameGatlingRequestDTO(List.of(List.of(1, 1, 1), List.of(1, 1, 1), List.of(1, 1, 1)));
        try {
            return Map.of("gameRequestJson", OBJECT_MAPPER.writeValueAsString(gameRequest));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize game data", e);
        }
    }

    // Scenario definitions
    private final ScenarioBuilder readOnlyScenario = scenario("Read Only Users")
            .feed(gameDataFeeder)
            .exec(
                    http("Get All Games")
                            .get("/v1/games")
                            .check(status().is(200))
                            .check(jsonPath("$").ofList())
                            .check(responseTimeInMillis().lte(1000))
            )
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(3));

    private final ScenarioBuilder writeHeavyScenario = scenario("Write Heavy Users")
            .feed(gameDataFeeder)
            .exec(
                    http("Create Game")
                            .post("/v1/games")
                            .body(StringBody("#{gameRequestJson}"))
                            .check(status().is(201))
                            .check(jsonPath("$.id").exists())
                            .check(jsonPath("$.minimalHealth").ofInt())
                            .check(responseTimeInMillis().lte(2000))
                            .check(jsonPath("$.id").saveAs("gameId"))
            )
            .pause(Duration.ofMillis(500), Duration.ofSeconds(2))
            .exec(
                    http("Verify Created Game in List")
                            .get("/v1/games")
                            .check(status().is(200))
                            .check(jsonPath("$[?(@.id == '#{gameId}')]").exists())
            );

    private final ScenarioBuilder mixedScenario = scenario("Mixed Operations")
            .feed(gameDataFeeder)
            .exec(
                    http("Initial Games List")
                            .get("/v1/games")
                            .check(status().is(200))
                            .check(jsonPath("$.size()").saveAs("initialCount"))
            )
            .pause(Duration.ofMillis(200), Duration.ofMillis(800))
            .exec(
                    http("Create New Game")
                            .post("/v1/games")
                            .body(StringBody("#{gameRequestJson}"))
                            .check(status().is(201))
                            .check(jsonPath("$.id").saveAs("newGameId"))
                            .check(jsonPath("$.minimalHealth").saveAs("gameHealth"))
            )
            .pause(Duration.ofMillis(100), Duration.ofMillis(500))
            .exec(
                    http("Verify Game Count Increased")
                            .get("/v1/games")
                            .check(status().is(200))
                            .check(jsonPath("$.size()").gte("#{initialCount}"))
            );

    private final ScenarioBuilder stressTestScenario = scenario("Stress Test")
            .feed(gameDataFeeder)
            .during(Duration.ofSeconds(TEST_DURATION))
            .on(
                    randomSwitch().on(
                            new Choice.WithWeight(60.0, CoreDsl.exec(
                                    HttpDsl.http("Stress - Get Games")
                                            .get("/v1/games")
                                            .check(HttpDsl.status().is(200))
                            )),
                            new Choice.WithWeight(40.0, exec(
                                    http("Stress - Create Game")
                                            .post("/v1/games")
                                            .body(StringBody("#{gameRequestJson}"))
                                            .check(status().is(201))
                            ))
                    )
            );

    private final ScenarioBuilder performanceScenario = scenario("Performance Validation")
            .feed(gameDataFeeder)
            .exec(
                    http("Performance - Get Games")
                            .get("/v1/games")
                            .check(status().is(200))
                            .check(responseTimeInMillis().lte(500))
            )
            .pause(Duration.ofMillis(100))
            .exec(
                    http("Performance - Create Game")
                            .post("/v1/games")
                            .body(StringBody("#{gameRequestJson}"))
                            .check(status().is(201))
                            .check(responseTimeInMillis().lte(1000))
            );

    {
        setUp(
                readOnlyScenario.injectOpen(
                        rampUsers(USERS / 2).during(Duration.ofSeconds(RAMP_DURATION)),
                        constantUsersPerSec(USERS / 4.0).during(Duration.ofSeconds(TEST_DURATION))
                ),
                writeHeavyScenario.injectOpen(
                        rampUsers(USERS / 4).during(Duration.ofSeconds(RAMP_DURATION + 10)),
                        constantUsersPerSec(USERS / 8.0).during(Duration.ofSeconds(TEST_DURATION))
                ),
                mixedScenario.injectOpen(
                        rampUsers(USERS / 3).during(Duration.ofSeconds(RAMP_DURATION + 5)),
                        constantUsersPerSec(USERS / 6.0).during(Duration.ofSeconds(TEST_DURATION))
                ),
                stressTestScenario.injectOpen(
                        nothingFor(Duration.ofSeconds(30)),
                        rampUsers(USERS).during(Duration.ofSeconds(20)),
                        constantUsersPerSec(USERS / 2.0).during(Duration.ofSeconds(60)),
                        rampUsers(USERS * 2).during(Duration.ofSeconds(30))
                ),
                performanceScenario.injectOpen(
                        rampUsers(10).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(5.0).during(Duration.ofSeconds(TEST_DURATION))
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lte(5000),
                        global().responseTime().mean().lte(1000),
                        global().responseTime().percentile3().lte(2000),
                        global().successfulRequests().percent().gte(95.0),

                        details("Get All Games").responseTime().percentile3().lte(800),
                        details("Create Game").responseTime().percentile3().lte(1500),
                        details("Create Game").successfulRequests().percent().gte(98.0),

                        details("Performance - Get Games").responseTime().max().lte(500),
                        details("Performance - Create Game").responseTime().max().lte(1000)
                );
    }
}