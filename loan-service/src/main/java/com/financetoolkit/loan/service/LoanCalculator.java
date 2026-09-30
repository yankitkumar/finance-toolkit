package com.financetoolkit.loan.service;

import com.financetoolkit.loan.dto.AmortizationRow;
import com.financetoolkit.loan.dto.LoanRequest;
import com.financetoolkit.loan.dto.LoanSummary;
import com.financetoolkit.loan.dto.PrepaymentResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * All loan math lives here, away from HTTP concerns, so it can be unit-tested
 * without starting Spring.
 *
 * Why BigDecimal and not double?
 * double stores numbers in binary, so 0.1 + 0.2 = 0.30000000000000004.
 * That is fine for physics but not for money, where every paisa/cent must
 * add up. BigDecimal stores exact decimal digits and makes us choose how to
 * round, just like a bank does.
 */
@Service
public class LoanCalculator {

    /** Precision for intermediate steps (16 significant digits). Money is rounded to 2 places at the end. */
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    /**
     * The fixed monthly payment (EMI) that clears the loan in exactly {@code years}.
     *
     * Formula:  EMI = P × i × (1 + i)^n / ((1 + i)^n − 1)
     *
     *   P = principal, i = monthly rate (annual % / 100 / 12), n = number of months
     *
     * Intuition: the bank is indifferent between getting P today and getting n
     * payments of EMI spread over time, once each payment is discounted back
     * to today. This formula is that balance solved for EMI.
     */
    public BigDecimal monthlyPayment(LoanRequest loan) {
        int n = loan.years() * 12;
        BigDecimal i = monthlyRate(loan.annualRatePercent());

        if (i.signum() == 0) {
            // Interest-free loan: just split the principal evenly.
            return loan.principal().divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }

        BigDecimal growth = BigDecimal.ONE.add(i).pow(n, MC);           // (1 + i)^n
        BigDecimal emi = loan.principal().multiply(i).multiply(growth)
                .divide(growth.subtract(BigDecimal.ONE), MC);
        return money(emi);
    }

    /** Headline numbers for the loan: EMI, total paid and total interest. */
    public LoanSummary summarize(LoanRequest loan) {
        return summaryOf(schedule(loan));
    }

    /** Month-by-month breakdown of the loan (the "amortization schedule"). */
    public List<AmortizationRow> schedule(LoanRequest loan) {
        return simulate(loan, monthlyPayment(loan));
    }

    /**
     * Compares the normal loan with one where {@code extra} is added to every EMI.
     *
     * The extra money goes 100% to principal, so next month's interest is
     * charged on a smaller balance. That snowball ends the loan early and
     * can save a surprising amount of interest.
     */
    public PrepaymentResult prepayment(LoanRequest loan, BigDecimal extra) {
        BigDecimal emi = monthlyPayment(loan);
        LoanSummary original = summaryOf(simulate(loan, emi));
        LoanSummary faster = summaryOf(simulate(loan, emi.add(extra)));

        return new PrepaymentResult(
                original.months(),
                faster.months(),
                original.months() - faster.months(),
                original.totalInterest(),
                faster.totalInterest(),
                original.totalInterest().subtract(faster.totalInterest()));
    }

    /**
     * Runs the loan month by month with a fixed {@code payment}.
     *
     * Each month:
     *   1. interest  = balance × monthly rate      (rounded to the cent)
     *   2. principal = payment − interest          (the part that reduces debt)
     *   3. balance   = balance − principal
     *
     * Because the EMI and each month's interest are rounded to the cent, a few
     * cents are left over (or overpaid) by the end. Real banks fix that in the
     * last payment, and so do we: the final payment is simply "whatever is
     * still owed". A payment is final when it can clear the balance, or when
     * the loan term runs out.
     */
    private List<AmortizationRow> simulate(LoanRequest loan, BigDecimal payment) {
        int maxMonths = loan.years() * 12;
        BigDecimal i = monthlyRate(loan.annualRatePercent());
        BigDecimal balance = money(loan.principal());

        List<AmortizationRow> rows = new ArrayList<>();
        int month = 0;
        while (balance.signum() > 0) {
            month++;
            BigDecimal interest = money(balance.multiply(i));
            BigDecimal owed = balance.add(interest);

            BigDecimal paid;
            BigDecimal principalPaid;
            if (owed.compareTo(payment) <= 0 || month == maxMonths) {
                // Final month: pay off everything that is left.
                paid = owed;
                principalPaid = balance;
            } else {
                paid = payment;
                principalPaid = payment.subtract(interest);
            }
            balance = balance.subtract(principalPaid);
            rows.add(new AmortizationRow(month, paid, interest, principalPaid, balance));
        }
        return rows;
    }

    /** Adds up a schedule into its headline numbers. */
    private LoanSummary summaryOf(List<AmortizationRow> rows) {
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        for (AmortizationRow row : rows) {
            totalPaid = totalPaid.add(row.payment());
            totalInterest = totalInterest.add(row.interest());
        }
        // The regular EMI is the first payment; only the last one may differ slightly.
        BigDecimal emi = rows.isEmpty() ? BigDecimal.ZERO : rows.get(0).payment();
        return new LoanSummary(emi, rows.size(), money(totalPaid), money(totalInterest));
    }

    /** 9 (% per year) → 0.0075 (per month). */
    static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent.divide(HUNDRED, MC).divide(TWELVE, MC);
    }

    /** Rounds to whole cents, the way money is actually paid. */
    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
