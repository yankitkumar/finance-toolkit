package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Example JSON: {"nominalRatePercent": 7, "inflationRatePercent": 5}
 *
 * @param nominalRatePercent   the rate your bank/fund advertises
 * @param inflationRatePercent how fast prices are rising
 */
public record RealReturnRequest(
        @NotNull @DecimalMin("-100") @DecimalMax("100") BigDecimal nominalRatePercent,
        @NotNull @DecimalMin(value = "-100", inclusive = false) @DecimalMax("100") BigDecimal inflationRatePercent) {
}
