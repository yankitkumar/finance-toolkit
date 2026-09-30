package com.financetoolkit.planner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * The savings-goal part of a plan. Field names match investment-service's
 * /goal request, so this object can be forwarded as-is.
 *
 * @param annualRatePercent the yearly return you expect your investments to earn
 */
public record GoalDetails(
        @NotNull @Positive BigDecimal targetAmount,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal annualRatePercent,
        @NotNull @Min(1) @Max(50) Integer years) {
}
