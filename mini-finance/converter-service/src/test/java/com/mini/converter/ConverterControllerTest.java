package com.mini.converter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConverterController.class)
class ConverterControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RateClient rateClient;

    @Test
    void convertsUsingRateFromRateService() throws Exception {
        when(rateClient.getRate("USD", "INR")).thenReturn(new BigDecimal("83.50"));

        mvc.perform(get("/convert").param("from", "usd").param("to", "inr").param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(8350.00));
    }

    @Test
    void rateServiceDownIs503() throws Exception {
        when(rateClient.getRate("USD", "INR"))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "rate-service is unavailable"));

        mvc.perform(get("/convert").param("from", "USD").param("to", "INR").param("amount", "1"))
                .andExpect(status().isServiceUnavailable());
    }
}
