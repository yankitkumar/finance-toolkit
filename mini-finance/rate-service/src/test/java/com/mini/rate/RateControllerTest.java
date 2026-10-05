package com.mini.rate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateController.class)
class RateControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void returnsKnownRate() throws Exception {
        mvc.perform(get("/rates/usd/inr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("INR"))
                .andExpect(jsonPath("$.rate").value(83.50));
    }

    @Test
    void unknownPairIs404() throws Exception {
        mvc.perform(get("/rates/JPY/INR")).andExpect(status().isNotFound());
    }
}
