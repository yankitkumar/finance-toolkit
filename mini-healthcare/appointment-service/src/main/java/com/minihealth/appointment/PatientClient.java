package com.minihealth.appointment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** The one place appointment-service talks to patient-service over HTTP. */
@Component
public class PatientClient {

    public record Patient(Long id, String name, int age) {
    }

    private final RestClient restClient;

    public PatientClient(RestClient.Builder builder, @Value("${patient-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Patient getPatient(Long id) {
        try {
            return restClient.get()
                    .uri("/patients/{id}", id)
                    .retrieve()
                    .body(Patient.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient " + id + " not found");
        } catch (RestClientException e) {
            // patient-service is down or broken: say so instead of failing with a vague 500.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "patient-service is unavailable");
        }
    }
}
