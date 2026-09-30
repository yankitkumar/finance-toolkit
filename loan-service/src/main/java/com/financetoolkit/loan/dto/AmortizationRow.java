package com.financetoolkit.loan.dto;

import java.math.BigDecimal;

/**
 * One month of a loan's life.
 *
 * payment = interest + principal. Early rows are mostly interest; later rows
 * are mostly principal, because interest is charged on a shrinking balance.
 *
 * @param month     1-based month number
 * @param payment   amount paid this month
 * @param interest  part of the payment that is the bank's charge
 * @param principal part of the payment that reduces the debt
 * @param balance   debt left after this payment
 */
public record AmortizationRow(
        int month,
        BigDecimal payment,
        BigDecimal interest,
        BigDecimal principal,
        BigDecimal balance) {
}
