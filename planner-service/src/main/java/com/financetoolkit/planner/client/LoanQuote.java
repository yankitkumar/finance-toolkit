package com.financetoolkit.planner.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * The part of loan-service's response that the planner needs.
 *
 * ignoreUnknown = true means loan-service can add new fields later without
 * breaking this service — an important habit when services evolve separately.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoanQuote(BigDecimal monthlyPayment, BigDecimal totalInterest) {
}
