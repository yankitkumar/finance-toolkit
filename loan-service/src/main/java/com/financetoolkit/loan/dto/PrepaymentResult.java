package com.financetoolkit.loan.dto;

import java.math.BigDecimal;

/**
 * Side-by-side comparison of the loan with and without the extra payment.
 */
public record PrepaymentResult(
        int originalMonths,
        int newMonths,
        int monthsSaved,
        BigDecimal originalInterest,
        BigDecimal newInterest,
        BigDecimal interestSaved) {
}
