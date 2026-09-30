package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/** What a one-time investment grows into, and how much of that is interest. */
public record LumpSumResult(BigDecimal invested, BigDecimal futureValue, BigDecimal interestEarned) {
}
