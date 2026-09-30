package com.financetoolkit.loan.service;

import com.financetoolkit.loan.dto.AmortizationRow;
import com.financetoolkit.loan.dto.LoanRequest;
import com.financetoolkit.loan.dto.LoanSummary;
import com.financetoolkit.loan.dto.PrepaymentResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests: no Spring context, so they run in milliseconds.
 * Each expected value can be checked with any online EMI calculator.
 */
class LoanCalculatorTest {

    private final LoanCalculator calculator = new LoanCalculator();

    private static LoanRequest loan(String principal, String ratePercent, int years) {
        return new LoanRequest(new BigDecimal(principal), new BigDecimal(ratePercent), years);
    }

    @Test
    void emiMatchesTextbookExample() {
        // 200,000 at 6% for 30 years is the classic mortgage example: 1,199.10 a month.
        assertThat(calculator.monthlyPayment(loan("200000", "6", 30))).isEqualByComparingTo("1199.10");
    }

    @Test
    void zeroInterestLoanIsAnEvenSplit() {
        LoanSummary summary = calculator.summarize(loan("12000", "0", 1));
        assertThat(summary.monthlyPayment()).isEqualByComparingTo("1000.00");
        assertThat(summary.totalInterest()).isEqualByComparingTo("0");
    }

    @Test
    void firstMonthIsMostlyInterest() {
        // 20 lakh at 9%: month 1 interest = 2,000,000 × 0.75% = 15,000 out of a 17,994.52 EMI.
        List<AmortizationRow> rows = calculator.schedule(loan("2000000", "9", 20));
        AmortizationRow first = rows.get(0);
        assertThat(first.payment()).isEqualByComparingTo("17994.52");
        assertThat(first.interest()).isEqualByComparingTo("15000.00");
        assertThat(first.principal()).isEqualByComparingTo("2994.52");

        AmortizationRow last = rows.get(rows.size() - 1);
        assertThat(last.interest()).isLessThan(last.principal());
    }

    @Test
    void everyScheduleLastsExactlyItsTermAndEndsAtZero() {
        // Sweep many loans so cent-rounding drift in either direction is covered.
        for (String principal : List.of("5000", "10000", "123457", "2000000")) {
            for (String rate : List.of("0", "3.5", "8", "12.5")) {
                for (int years : List.of(1, 3, 15, 30)) {
                    List<AmortizationRow> rows = calculator.schedule(loan(principal, rate, years));

                    assertThat(rows).hasSize(years * 12);
                    assertThat(rows.get(rows.size() - 1).balance()).isEqualByComparingTo("0");
                    BigDecimal principalRepaid = rows.stream()
                            .map(AmortizationRow::principal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(principalRepaid).isEqualByComparingTo(principal);
                }
            }
        }
    }

    @Test
    void totalPaymentIsPrincipalPlusInterest() {
        LoanSummary summary = calculator.summarize(loan("500000", "10", 5));
        assertThat(summary.totalPayment())
                .isEqualByComparingTo(summary.totalInterest().add(new BigDecimal("500000")));
    }

    @Test
    void extraPaymentShortensLoanAndSavesInterest() {
        PrepaymentResult result = calculator.prepayment(loan("2000000", "9", 20), new BigDecimal("5000"));
        assertThat(result.originalMonths()).isEqualTo(240);
        assertThat(result.newMonths()).isLessThan(240);
        assertThat(result.monthsSaved()).isEqualTo(result.originalMonths() - result.newMonths());
        assertThat(result.interestSaved()).isPositive();
    }

    @Test
    void noExtraPaymentChangesNothing() {
        PrepaymentResult result = calculator.prepayment(loan("10000", "8", 3), BigDecimal.ZERO);
        assertThat(result.newMonths()).isEqualTo(36);
        assertThat(result.monthsSaved()).isZero();
        assertThat(result.interestSaved()).isEqualByComparingTo("0");
    }
}
