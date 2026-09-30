package com.financetoolkit.loan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * "What if I pay a little extra every month?"
 *
 * Example JSON:
 * {"loan": {"principal": 2000000, "annualRatePercent": 9, "years": 20}, "extraMonthlyPayment": 5000}
 *
 * {@code @Valid} tells the validator to also check the fields inside {@code loan}.
 */
public record PrepaymentRequest(
        @NotNull @Valid LoanRequest loan,
        @NotNull @PositiveOrZero BigDecimal extraMonthlyPayment) {
}
