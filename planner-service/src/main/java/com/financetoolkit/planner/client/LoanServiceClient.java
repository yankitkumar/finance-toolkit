package com.financetoolkit.planner.client;

import com.financetoolkit.planner.dto.LoanDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls loan-service over HTTP: POST /api/loans/emi.
 *
 * The base URL comes from configuration (services.loan.base-url), so the same
 * code works on a laptop (localhost:8081) and in Docker (loan-service:8081).
 */
@Component
public class LoanServiceClient {

    private final RestClient restClient;

    // Spring Boot provides a pre-configured RestClient.Builder (JSON support included).
    public LoanServiceClient(RestClient.Builder builder, @Value("${services.loan.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public LoanQuote quote(LoanDetails loan) {
        try {
            return restClient.post()
                    .uri("/api/loans/emi")
                    .body(loan)                 // Java object → JSON request body
                    .retrieve()                 // 4xx/5xx responses become exceptions
                    .body(LoanQuote.class);     // JSON response → Java object
        } catch (RestClientException e) {
            throw new DownstreamServiceException("loan-service", e);
        }
    }
}
