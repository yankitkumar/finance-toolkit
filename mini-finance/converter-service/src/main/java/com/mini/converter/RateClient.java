package com.mini.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/** The one place converter-service talks to rate-service over HTTP. */
@Component
public class RateClient {

    record Rate(String from, String to, BigDecimal rate) {
    }

    private final RestClient restClient;

    public RateClient(RestClient.Builder builder, @Value("${rate-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public BigDecimal getRate(String from, String to) {
        try {
            Rate rate = restClient.get()
                    .uri("/rates/{from}/{to}", from, to)
                    .retrieve()
                    .body(Rate.class);
            return rate.rate();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No rate for " + from + " -> " + to);
        } catch (RestClientException e) {
            // rate-service is down or broken: say so instead of failing with a vague 500.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "rate-service is unavailable");
        }
    }
}
