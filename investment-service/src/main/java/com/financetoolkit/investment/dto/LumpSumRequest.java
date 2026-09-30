package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * A one-time investment left to grow.
 *
 * Example JSON: {"amount": 100000, "annualRatePercent": 8, "years": 10, "compoundsPerYear": 12}
 *
 * @param compoundsPerYear how often interest is added: 1 = yearly, 4 = quarterly, 12 = monthly
 */
public record LumpSumRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years,
        @NotNull @Min(1) @Max(365) Integer compoundsPerYear) {
}
