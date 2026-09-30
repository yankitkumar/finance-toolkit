package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * An investment described as yearly cash flows.
 *
 * Sign convention: money you pay out is negative, money you receive is positive.
 * cashFlows[0] happens today, cashFlows[1] in one year, and so on.
 *
 * Example JSON: {"discountRatePercent": 10, "cashFlows": [-100000, 30000, 30000, 30000, 30000, 30000]}
 *
 * @param discountRatePercent the return you could get elsewhere (your "required return")
 */
public record ProjectRequest(
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal discountRatePercent,
        @NotNull @Size(min = 2, max = 100) List<@NotNull BigDecimal> cashFlows) {
}
