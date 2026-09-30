package com.financetoolkit.loan.dto;

import java.math.BigDecimal;

/**
 * The headline numbers of a loan.
 *
 * @param monthlyPayment the fixed EMI paid every month
 * @param months         number of payments
 * @param totalPayment   everything you hand the bank over the whole loan
 * @param totalInterest  the true cost of borrowing: totalPayment - principal
 */
public record LoanSummary(
        BigDecimal monthlyPayment,
        int months,
        BigDecimal totalPayment,
        BigDecimal totalInterest) {
}
