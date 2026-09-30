package com.financetoolkit.planner.client;

import com.financetoolkit.planner.dto.GoalDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls investment-service over HTTP: POST /api/investments/goal. */
@Component
public class InvestmentServiceClient {

    private final RestClient restClient;

    public InvestmentServiceClient(RestClient.Builder builder,
                                   @Value("${services.investment.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public GoalQuote quote(GoalDetails goal) {
        try {
            return restClient.post()
                    .uri("/api/investments/goal")
                    .body(goal)
                    .retrieve()
                    .body(GoalQuote.class);
        } catch (RestClientException e) {
            throw new DownstreamServiceException("investment-service", e);
        }
    }
}
