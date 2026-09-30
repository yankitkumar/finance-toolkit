package com.financetoolkit.investment.controller;

import com.financetoolkit.investment.service.InvestmentCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvestmentController.class)
@Import(InvestmentCalculator.class)
class InvestmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculatesGoal() throws Exception {
        mockMvc.perform(post("/api/investments/goal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetAmount": 1000000, "annualRatePercent": 12, "years": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyInvestment").isNumber());
    }

    @Test
    void financiallyImpossibleInputIsA400NotA500() throws Exception {
        // Only positive cash flows: there is no "return" without first investing something.
        mockMvc.perform(post("/api/investments/evaluate-project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discountRatePercent": 10, "cashFlows": [100, 200, 300]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("outflow")));
    }

    @Test
    void missingFieldsAreListed() throws Exception {
        mockMvc.perform(post("/api/investments/sip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.monthlyInvestment").exists())
                .andExpect(jsonPath("$.errors.annualRatePercent").exists())
                .andExpect(jsonPath("$.errors.years").exists());
    }
}
