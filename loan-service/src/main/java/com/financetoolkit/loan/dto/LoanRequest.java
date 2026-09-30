package com.financetoolkit.loan.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * The three numbers that define a standard loan.
 *
 * Example JSON: {"principal": 2000000, "annualRatePercent": 9, "years": 20}
 *
 * @param principal         amount borrowed
 * @param annualRatePercent yearly interest rate in percent (9 means 9%, not 0.09)
 * @param years             loan tenure; repaid monthly, so months = years * 12
 */
public record LoanRequest(
        @NotNull @Positive BigDecimal principal,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years) {
}
