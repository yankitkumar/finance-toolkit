package com.financetoolkit.investment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Investment Service: answers "how will my money grow?" and "is this investment worth it?".
 *
 * Stateless like the loan service: no database, every answer comes from the request.
 */
@SpringBootApplication
public class InvestmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvestmentServiceApplication.class, args);
    }
}
