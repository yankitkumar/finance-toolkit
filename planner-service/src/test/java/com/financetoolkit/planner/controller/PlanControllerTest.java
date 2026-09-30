package com.financetoolkit.planner.controller;

import com.financetoolkit.planner.client.DownstreamServiceException;
import com.financetoolkit.planner.client.GoalQuote;
import com.financetoolkit.planner.client.InvestmentServiceClient;
import com.financetoolkit.planner.client.LoanQuote;
import com.financetoolkit.planner.client.LoanServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.ConnectException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Starts the whole planner-service (controllers, service, JPA and the real H2
 * database) but replaces the two HTTP clients with @MockBean fakes, so the
 * other services don't need to be running.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlanControllerTest {

    private static final String PLAN_JSON = """
            {
              "name": "Home + education",
              "monthlyIncome": 150000,
              "monthlyExpenses": 60000,
              "loan": {"principal": 3000000, "annualRatePercent": 8.5, "years": 20},
              "goal": {"targetAmount": 2000000, "annualRatePercent": 12, "years": 10}
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanServiceClient loanClient;

    @MockBean
    private InvestmentServiceClient investmentClient;

    @Test
    void createsSavesAndReturnsPlan() throws Exception {
        when(loanClient.quote(any())).thenReturn(new LoanQuote(new BigDecimal("26034.55"), null));
        when(investmentClient.quote(any())).thenReturn(new GoalQuote(new BigDecimal("8608.02")));

        String location = mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLAN_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.monthlyLeftover").value(55357.43))
                .andExpect(jsonPath("$.verdict").value("AFFORDABLE"))
                .andReturn().getResponse().getHeader("Location");

        // The plan was really saved: we can read it back from the database.
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Home + education"));
    }

    @Test
    void unknownPlanIs404() throws Exception {
        mockMvc.perform(get("/api/plans/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No plan with id 999999"));
    }

    @Test
    void nestedFieldErrorsAreReported() throws Exception {
        mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLAN_JSON.replace("\"years\": 20", "\"years\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['loan.years']").exists());
    }

    @Test
    void downServiceGives503() throws Exception {
        when(loanClient.quote(any()))
                .thenThrow(new DownstreamServiceException("loan-service", new ConnectException("Connection refused")));

        mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLAN_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.service").value("loan-service"));
    }
}
