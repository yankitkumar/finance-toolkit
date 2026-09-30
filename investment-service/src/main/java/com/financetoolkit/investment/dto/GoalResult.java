package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/**
 * @param monthlyInvestment  amount to invest every month (rounded up so you never fall short)
 * @param totalInvested      monthlyInvestment × number of months
 * @param growthContribution how much of the goal compounding pays for
 */
public record GoalResult(BigDecimal monthlyInvestment, BigDecimal totalInvested, BigDecimal growthContribution) {
}
