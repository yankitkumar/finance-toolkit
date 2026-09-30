package com.financetoolkit.planner.service;

import com.financetoolkit.planner.client.InvestmentServiceClient;
import com.financetoolkit.planner.client.LoanServiceClient;
import com.financetoolkit.planner.dto.PlanRequest;
import com.financetoolkit.planner.model.FinancialPlan;
import com.financetoolkit.planner.model.PlanVerdict;
import com.financetoolkit.planner.repository.FinancialPlanRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Builds a plan in three steps:
 *   1. ask loan-service for the EMI                    (HTTP call)
 *   2. ask investment-service for the monthly SIP      (HTTP call)
 *   3. compare both against the budget and save it     (local database)
 */
@Service
public class PlannerService {

    /** Lenders get nervous when EMIs take more than ~40% of income (often called FOIR). */
    static final BigDecimal MAX_SAFE_EMI_PERCENT = BigDecimal.valueOf(40);

    /** Keep at least 10% of income unallocated as a buffer for surprises. */
    static final BigDecimal MIN_BUFFER_PERCENT = BigDecimal.valueOf(10);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final LoanServiceClient loanClient;
    private final InvestmentServiceClient investmentClient;
    private final FinancialPlanRepository repository;

    public PlannerService(LoanServiceClient loanClient,
                          InvestmentServiceClient investmentClient,
                          FinancialPlanRepository repository) {
        this.loanClient = loanClient;
        this.investmentClient = investmentClient;
        this.repository = repository;
    }

    public FinancialPlan createPlan(PlanRequest request) {
        BigDecimal emi = loanClient.quote(request.loan()).monthlyPayment();
        BigDecimal sip = investmentClient.quote(request.goal()).monthlyInvestment();
        return repository.save(assess(request, emi, sip));
    }

    public Optional<FinancialPlan> findPlan(Long id) {
        return repository.findById(id);
    }

    public List<FinancialPlan> allPlans() {
        return repository.findAll();
    }

    /**
     * The budgeting rules, kept free of HTTP and database code so they're easy
     * to read and to unit-test.
     *
     * Checks run from most to least serious, and the first one that matches wins:
     *   1. Leftover below zero      → NOT_AFFORDABLE (the numbers simply don't fit)
     *   2. EMI above 40% of income  → RISKY          (fits today, fragile tomorrow)
     *   3. Leftover below 10%       → TIGHT          (fits, but no safety buffer)
     *   4. Otherwise                → AFFORDABLE
     */
    FinancialPlan assess(PlanRequest request, BigDecimal emi, BigDecimal sip) {
        BigDecimal income = request.monthlyIncome();
        BigDecimal leftover = income.subtract(request.monthlyExpenses()).subtract(emi).subtract(sip);
        BigDecimal emiPercent = percentOf(emi, income);
        BigDecimal leftoverPercent = percentOf(leftover, income);

        PlanVerdict verdict;
        String advice;
        if (leftover.signum() < 0) {
            verdict = PlanVerdict.NOT_AFFORDABLE;
            advice = "You'd be short by %s every month. Try a smaller loan, a longer tenure, a smaller goal or more years to reach it."
                    .formatted(leftover.negate());
        } else if (emiPercent.compareTo(MAX_SAFE_EMI_PERCENT) > 0) {
            verdict = PlanVerdict.RISKY;
            advice = "The EMI takes %s%% of your income. Lenders prefer under %s%%; a job loss or rate rise would hit hard."
                    .formatted(emiPercent, MAX_SAFE_EMI_PERCENT);
        } else if (leftoverPercent.compareTo(MIN_BUFFER_PERCENT) < 0) {
            verdict = PlanVerdict.TIGHT;
            advice = "It fits, but only %s (%s%% of income) is left each month. Build an emergency fund of about 6 months' expenses first."
                    .formatted(leftover, leftoverPercent);
        } else {
            verdict = PlanVerdict.AFFORDABLE;
            advice = "Comfortable: %s (%s%% of income) is left every month after the EMI and your investment."
                    .formatted(leftover, leftoverPercent);
        }

        return new FinancialPlan(request.name(), income, request.monthlyExpenses(),
                emi, sip, leftover, emiPercent, verdict, advice);
    }

    /** part / whole × 100, rounded to 2 decimals. */
    private static BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        return part.multiply(HUNDRED).divide(whole, 2, RoundingMode.HALF_UP);
    }
}
