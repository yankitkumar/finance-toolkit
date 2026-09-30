package com.financetoolkit.planner.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** The part of investment-service's /goal response that the planner needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GoalQuote(BigDecimal monthlyInvestment) {
}
