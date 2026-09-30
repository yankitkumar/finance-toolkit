package com.financetoolkit.planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Everything needed to judge whether a loan and a savings goal fit a budget.
 *
 * Example JSON:
 * {
 *   "name": "Home + child education",
 *   "monthlyIncome": 150000,
 *   "monthlyExpenses": 60000,
 *   "loan": {"principal": 3000000, "annualRatePercent": 8.5, "years": 20},
 *   "goal": {"targetAmount": 2000000, "annualRatePercent": 12, "years": 10}
 * }
 */
public record PlanRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive BigDecimal monthlyIncome,
        @NotNull @PositiveOrZero BigDecimal monthlyExpenses,
        @NotNull @Valid LoanDetails loan,
        @NotNull @Valid GoalDetails goal) {
}
