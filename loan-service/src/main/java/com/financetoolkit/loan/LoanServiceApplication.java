package com.financetoolkit.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Loan Service: answers "what does this loan really cost me?".
 *
 * It is stateless — every answer is calculated from the request alone, so it
 * needs no database and any number of copies can run side by side.
 */
@SpringBootApplication
public class LoanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
    }
}
