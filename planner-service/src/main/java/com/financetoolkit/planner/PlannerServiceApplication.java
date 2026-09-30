package com.financetoolkit.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Planner Service: answers "can I afford this loan AND still reach my savings goal?".
 *
 * This is the service that shows what makes microservices different:
 *   - it doesn't do loan or investment math itself — it calls loan-service
 *     and investment-service over HTTP and combines their answers;
 *   - it owns its own database (H2) holding the plans it creates; no other
 *     service reads that database directly.
 */
@SpringBootApplication
public class PlannerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlannerServiceApplication.class, args);
    }
}
