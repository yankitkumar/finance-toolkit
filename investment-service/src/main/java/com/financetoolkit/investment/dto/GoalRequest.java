package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * "I want targetAmount in N years — how much should I invest each month?"
 *
 * Example JSON: {"targetAmount": 1000000, "annualRatePercent": 12, "years": 5}
 *
 * @param annualRatePercent the return you expect your investments to earn
 */
public record GoalRequest(
        @NotNull @Positive BigDecimal targetAmount,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years) {
}
