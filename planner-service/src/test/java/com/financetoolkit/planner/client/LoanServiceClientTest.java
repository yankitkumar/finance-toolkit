package com.financetoolkit.planner.client;

import com.financetoolkit.planner.dto.LoanDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests the HTTP call itself. MockRestServiceServer pretends to be loan-service:
 * it checks what request we send and replies with a canned response.
 */
@RestClientTest(components = LoanServiceClient.class, properties = "services.loan.base-url=http://loan-service")
class LoanServiceClientTest {

    @Autowired
    private LoanServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    private final LoanDetails loan = new LoanDetails(new BigDecimal("200000"), new BigDecimal("6"), 30);

    @Test
    void sendsLoanAsJsonAndReadsEmi() {
        server.expect(requestTo("http://loan-service/api/loans/emi"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.principal").value(200000))
                .andExpect(jsonPath("$.years").value(30))
                .andRespond(withSuccess("""
                        {"monthlyPayment": 1199.10, "months": 360, "totalPayment": 431676.38, "totalInterest": 231676.38}
                        """, MediaType.APPLICATION_JSON));

        LoanQuote quote = client.quote(loan);

        assertThat(quote.monthlyPayment()).isEqualByComparingTo("1199.10");
        server.verify();
    }

    @Test
    void serverErrorBecomesDownstreamServiceException() {
        server.expect(requestTo("http://loan-service/api/loans/emi")).andRespond(withServerError());

        assertThatThrownBy(() -> client.quote(loan))
                .isInstanceOf(DownstreamServiceException.class)
                .hasMessageContaining("loan-service");
    }
}
