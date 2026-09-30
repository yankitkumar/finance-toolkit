package com.financetoolkit.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the real gateway on a random port, with two services pointed at
 * addresses that can't work, to see what a caller gets when a service is down:
 *   loan-service       → port 1, where nothing listens         (connection refused)
 *   investment-service → a ".invalid" host, which never resolves (unknown host)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "LOAN_SERVICE_URL=http://127.0.0.1:1",
                "INVESTMENT_SERVICE_URL=http://investment-service.invalid:8082"
        })
class ApiGatewayApplicationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private WebTestClient webClient;

    @Test
    void hasARouteForEveryService() {
        List<String> routeIds = routeLocator.getRoutes().map(Route::getId).collectList().block();
        assertThat(routeIds).containsExactlyInAnyOrder("loan-service", "investment-service", "planner-service");
    }

    @Test
    void refusedConnectionGives503NamingTheService() {
        webClient.post().uri("/api/loans/emi")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"principal\": 1000, \"annualRatePercent\": 5, \"years\": 1}")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.service").isEqualTo("loan-service");
    }

    @Test
    void unknownHostGives503NamingTheService() {
        webClient.post().uri("/api/investments/cagr")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"startValue\": 100, \"endValue\": 200, \"years\": 5}")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.service").isEqualTo("investment-service");
    }
}
