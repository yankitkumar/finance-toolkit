package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/** The single steady yearly growth rate that turns startValue into endValue. */
public record CagrResult(BigDecimal cagrPercent) {
}
