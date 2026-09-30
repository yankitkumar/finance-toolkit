package com.financetoolkit.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway: the single front door of the system.
 *
 * Clients only ever talk to port 8080. The gateway looks at the URL and
 * forwards the request to the service that owns it (see application.yml):
 *
 *   /api/loans/**        → loan-service        (8081)
 *   /api/investments/**  → investment-service  (8082)
 *   /api/plans/**        → planner-service     (8083)
 *
 * Why bother? Clients don't need to know how many services exist or where
 * they run, and cross-cutting concerns (auth, rate limits, CORS, logging)
 * can be added here once instead of in every service.
 *
 * There is no Java routing code: Spring Cloud Gateway reads the routes from
 * configuration.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
