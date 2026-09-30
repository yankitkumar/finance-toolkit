package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * A SIP (Systematic Investment Plan): the same amount invested every month.
 *
 * Example JSON: {"monthlyInvestment": 10000, "annualRatePercent": 12, "years": 10}
 */
public record SipRequest(
        @NotNull @Positive BigDecimal monthlyInvestment,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years) {
}
