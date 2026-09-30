package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/**
 * @param totalInvested what you put in from your pocket
 * @param futureValue   what the investments are worth at the end
 * @param wealthGained  the part created by compounding: futureValue - totalInvested
 */
public record SipResult(BigDecimal totalInvested, BigDecimal futureValue, BigDecimal wealthGained) {
}
