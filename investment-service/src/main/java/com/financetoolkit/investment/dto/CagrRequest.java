package com.financetoolkit.investment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Example JSON: {"startValue": 100000, "endValue": 200000, "years": 5}
 */
public record CagrRequest(
        @NotNull @Positive BigDecimal startValue,
        @NotNull @Positive BigDecimal endValue,
        @NotNull @Min(1) @Max(100) Integer years) {
}
