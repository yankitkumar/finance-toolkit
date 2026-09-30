package com.financetoolkit.planner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * The loan part of a plan. Field names match loan-service's request, so this
 * object can be forwarded to loan-service as-is.
 */
public record LoanDetails(
        @NotNull @Positive BigDecimal principal,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years) {
}
