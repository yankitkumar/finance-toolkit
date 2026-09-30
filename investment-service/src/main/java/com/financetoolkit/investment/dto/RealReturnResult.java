package com.financetoolkit.investment.dto;

import java.math.BigDecimal;

/**
 * @param realRatePercent     how fast your purchasing power actually grows (exact)
 * @param shortcutRatePercent the popular "nominal − inflation" shortcut, shown for comparison
 */
public record RealReturnResult(BigDecimal realRatePercent, BigDecimal shortcutRatePercent) {
}
