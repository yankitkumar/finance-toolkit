package com.minihealth.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    private static final String BODY = "{\"patientId\":1,\"doctor\":\"Dr. Sen\",\"time\":\"2026-11-01T10:30:00\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PatientClient patientClient;

    @Test
    void booksAppointmentForKnownPatient() throws Exception {
        when(patientClient.getPatient(1L)).thenReturn(new PatientClient.Patient(1L, "Asha Rao", 34));

        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName").value("Asha Rao"))
                .andExpect(jsonPath("$.doctor").value("Dr. Sen"));
    }

    @Test
    void unknownPatientIs404() throws Exception {
        when(patientClient.getPatient(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient 1 not found"));

        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void patientServiceDownIs503() throws Exception {
        when(patientClient.getPatient(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "patient-service is unavailable"));

        mvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isServiceUnavailable());
    }
}
