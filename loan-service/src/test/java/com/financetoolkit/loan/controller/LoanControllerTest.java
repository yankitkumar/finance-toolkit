package com.financetoolkit.loan.controller;

import com.financetoolkit.loan.service.LoanCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest starts only the web layer (controllers, JSON, validation) — no
 * server port — and MockMvc sends fake HTTP requests to it.
 */
@WebMvcTest(LoanController.class)
@Import(LoanCalculator.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculatesEmi() throws Exception {
        mockMvc.perform(post("/api/loans/emi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"principal": 200000, "annualRatePercent": 6, "years": 30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyPayment").value(1199.10))
                .andExpect(jsonPath("$.months").value(360));
    }

    @Test
    void returnsScheduleWithOneRowPerMonth() throws Exception {
        mockMvc.perform(post("/api/loans/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"principal": 10000, "annualRatePercent": 8, "years": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule.length()").value(12))
                .andExpect(jsonPath("$.schedule[11].balance").value(0));
    }

    @Test
    void rejectsInvalidInputWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/loans/emi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"principal": -5, "annualRatePercent": 9, "years": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.principal").exists())
                .andExpect(jsonPath("$.errors.years").exists());
    }
}
