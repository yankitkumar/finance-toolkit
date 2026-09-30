package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/**
 * @param npv         the project's value in today's money after paying for the required return
 * @param irrPercent  the yearly return the project itself earns
 * @param decision    INVEST when NPV is zero or positive, otherwise REJECT
 * @param explanation the same verdict in plain words
 */
public record ProjectEvaluation(BigDecimal npv, BigDecimal irrPercent, String decision, String explanation) {
}
