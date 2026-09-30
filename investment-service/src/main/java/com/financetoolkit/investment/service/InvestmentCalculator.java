package com.financetoolkit.investment.service;

import com.financetoolkit.investment.dto.CagrRequest;
import com.financetoolkit.investment.dto.CagrResult;
import com.financetoolkit.investment.dto.GoalRequest;
import com.financetoolkit.investment.dto.GoalResult;
import com.financetoolkit.investment.dto.LumpSumRequest;
import com.financetoolkit.investment.dto.LumpSumResult;
import com.financetoolkit.investment.dto.ProjectEvaluation;
import com.financetoolkit.investment.dto.ProjectRequest;
import com.financetoolkit.investment.dto.RealReturnRequest;
import com.financetoolkit.investment.dto.RealReturnResult;
import com.financetoolkit.investment.dto.SipRequest;
import com.financetoolkit.investment.dto.SipResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * The finance math behind the investment endpoints.
 *
 * The one idea underneath everything here is the *time value of money*:
 * money can earn a return, so money today is worth more than the same amount
 * later. Growing money forward is "compounding"; pulling future money back to
 * today's value is "discounting". Both use the factor (1 + rate)^periods.
 *
 * Money uses BigDecimal (exact decimals). CAGR and IRR use double, because
 * they need fractional powers or trial-and-error search, which BigDecimal
 * doesn't support; their results are rates, rounded before returning.
 */
@Service
public class InvestmentCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    /**
     * Compound interest on a one-time investment.
     *
     * Formula:  FV = PV × (1 + r/m)^(m × t)
     *
     *   PV = amount, r = yearly rate, m = compounding periods per year, t = years
     *
     * "Compound" = you earn interest on your interest, so growth speeds up
     * over time. More frequent compounding (bigger m) gives slightly more.
     */
    public LumpSumResult lumpSum(LumpSumRequest request) {
        int m = request.compoundsPerYear();
        int periods = m * request.years();
        BigDecimal ratePerPeriod = fraction(request.annualRatePercent()).divide(BigDecimal.valueOf(m), MC);

        BigDecimal futureValue = request.amount().multiply(BigDecimal.ONE.add(ratePerPeriod).pow(periods, MC));
        return new LumpSumResult(
                money(request.amount()),
                money(futureValue),
                money(futureValue.subtract(request.amount())));
    }

    /**
     * What a monthly SIP grows into.
     *
     * Formula:  FV = P × [((1 + i)^n − 1) / i] × (1 + i)
     *
     *   P = monthly amount, i = monthly rate, n = number of months
     *
     * Each instalment grows for a different length of time: the first one for
     * all n months, the last one for just one month. The bracket adds up all
     * of those growth amounts; the extra × (1 + i) is there because a SIP is
     * invested at the *start* of each month (an "annuity due").
     */
    public SipResult sip(SipRequest request) {
        int n = request.years() * 12;
        BigDecimal i = monthlyRate(request.annualRatePercent());

        BigDecimal futureValue = request.monthlyInvestment().multiply(sipFactor(i, n));
        BigDecimal invested = request.monthlyInvestment().multiply(BigDecimal.valueOf(n));
        return new SipResult(money(invested), money(futureValue), money(futureValue.subtract(invested)));
    }

    /**
     * The SIP formula run backwards: the monthly amount that reaches a goal.
     *
     * Since  goal = monthly × sipFactor,  we get  monthly = goal ÷ sipFactor.
     *
     * We round *up* to the next cent: rounding down could leave you a little
     * short of your goal.
     */
    public GoalResult goal(GoalRequest request) {
        int n = request.years() * 12;
        BigDecimal i = monthlyRate(request.annualRatePercent());

        BigDecimal monthly = request.targetAmount().divide(sipFactor(i, n), MC).setScale(2, RoundingMode.CEILING);
        BigDecimal invested = monthly.multiply(BigDecimal.valueOf(n));
        // Rounding up can make "invested" a few cents more than the goal at 0% return; show 0 growth, not negative.
        BigDecimal growth = request.targetAmount().subtract(invested).max(BigDecimal.ZERO);
        return new GoalResult(monthly, money(invested), money(growth));
    }

    /**
     * Return after inflation (the Fisher equation).
     *
     * Formula:  real = (1 + nominal) / (1 + inflation) − 1
     *
     * People often use "nominal − inflation" as a shortcut. It's close, but it
     * overstates the result a little; we return both so you can compare.
     * Example: 7% while prices rise 5% → about 1.90% real, not 2%.
     */
    public RealReturnResult realReturn(RealReturnRequest request) {
        BigDecimal nominal = fraction(request.nominalRatePercent());
        BigDecimal inflation = fraction(request.inflationRatePercent());

        BigDecimal real = BigDecimal.ONE.add(nominal).divide(BigDecimal.ONE.add(inflation), MC).subtract(BigDecimal.ONE);
        return new RealReturnResult(
                percent(real),
                request.nominalRatePercent().subtract(request.inflationRatePercent()).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Compound Annual Growth Rate.
     *
     * Formula:  CAGR = (end / start)^(1 / years) − 1
     *
     * Real investments go up and down. CAGR is the one steady yearly rate that
     * would have produced the same result, which makes different investments
     * easy to compare.
     */
    public CagrResult cagr(CagrRequest request) {
        double ratio = request.endValue().doubleValue() / request.startValue().doubleValue();
        double cagr = Math.pow(ratio, 1.0 / request.years()) - 1;
        return new CagrResult(percent(BigDecimal.valueOf(cagr)));
    }

    /**
     * Should we invest? Uses NPV and IRR together.
     *
     * NPV (Net Present Value): convert every cash flow to today's money and add them up.
     *     NPV = Σ CF_t / (1 + r)^t
     *   NPV > 0 → the project beats your required return r → invest.
     *
     * IRR (Internal Rate of Return): the rate r that makes NPV exactly 0,
     *   i.e. the return the project itself earns. Invest if IRR > your required return.
     */
    public ProjectEvaluation evaluateProject(ProjectRequest request) {
        List<BigDecimal> flows = request.cashFlows();
        boolean hasOutflow = flows.stream().anyMatch(cf -> cf.signum() < 0);
        boolean hasInflow = flows.stream().anyMatch(cf -> cf.signum() > 0);
        if (!hasOutflow || !hasInflow) {
            throw new IllegalArgumentException(
                    "cashFlows need at least one outflow (negative) and one inflow (positive)");
        }

        BigDecimal npv = money(npv(fraction(request.discountRatePercent()), flows));
        BigDecimal irr = percent(BigDecimal.valueOf(irr(flows)));

        boolean invest = npv.signum() >= 0;
        String explanation = invest
                ? "NPV is %s: after earning your required %s%%, the project still adds %s in today's money. Its IRR of %s%% is at least your required return."
                        .formatted(npv.signum() == 0 ? "zero" : "positive", request.discountRatePercent(), npv, irr)
                : "NPV is negative: the project earns less than your required %s%% and would lose %s in today's money. Its IRR is only %s%%."
                        .formatted(request.discountRatePercent(), npv.negate(), irr);
        return new ProjectEvaluation(npv, irr, invest ? "INVEST" : "REJECT", explanation);
    }

    /** NPV = Σ CF_t / (1 + r)^t, where t is the index of the cash flow (0 = today). */
    BigDecimal npv(BigDecimal rate, List<BigDecimal> flows) {
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
        BigDecimal total = BigDecimal.ZERO;
        for (int t = 0; t < flows.size(); t++) {
            total = total.add(flows.get(t).divide(onePlusRate.pow(t, MC), MC));
        }
        return total;
    }

    /**
     * IRR by bisection ("guess the middle, keep the half with the answer").
     *
     * There's no formula for IRR, so we search. NPV goes down as the rate goes
     * up, so if NPV is positive at a low rate and negative at a high rate, the
     * IRR is somewhere in between. Test the midpoint, keep the half where the
     * sign still flips, and repeat until the range is tiny.
     */
    double irr(List<BigDecimal> flows) {
        double low = -0.99;   // -99%: can't lose more than everything
        double high = 10.0;   // 1000%: plenty for realistic projects
        // Compare signs only (Math.signum): multiplying two huge NPVs could overflow.
        double npvLow = npvAsDouble(low, flows);
        if (Math.signum(npvLow) * Math.signum(npvAsDouble(high, flows)) > 0) {
            throw new IllegalArgumentException("IRR is outside the -99% to 1000% range for these cash flows");
        }

        for (int step = 0; step < 200 && high - low > 1e-10; step++) {
            double mid = (low + high) / 2;
            double npvMid = npvAsDouble(mid, flows);
            if (Math.signum(npvLow) * Math.signum(npvMid) <= 0) {
                high = mid;              // sign flips in the lower half
            } else {
                low = mid;               // sign flips in the upper half
                npvLow = npvMid;
            }
        }
        return (low + high) / 2;
    }

    private static double npvAsDouble(double rate, List<BigDecimal> flows) {
        double total = 0;
        for (int t = 0; t < flows.size(); t++) {
            total += flows.get(t).doubleValue() / Math.pow(1 + rate, t);
        }
        return total;
    }

    /**
     * Value of investing 1 at the start of every month for n months:
     * ((1 + i)^n − 1) / i × (1 + i). With a 0% return it's simply n.
     */
    private static BigDecimal sipFactor(BigDecimal i, int n) {
        if (i.signum() == 0) {
            return BigDecimal.valueOf(n);
        }
        BigDecimal growth = BigDecimal.ONE.add(i).pow(n, MC);
        return growth.subtract(BigDecimal.ONE).divide(i, MC).multiply(BigDecimal.ONE.add(i), MC);
    }

    /** 12 (% per year) → 0.01 (per month). */
    private static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return fraction(annualRatePercent).divide(TWELVE, MC);
    }

    /** 8 (percent) → 0.08 (fraction). */
    private static BigDecimal fraction(BigDecimal percent) {
        return percent.divide(HUNDRED, MC);
    }

    /** 0.0829 (fraction) → 8.29 (percent, 2 decimals). */
    private static BigDecimal percent(BigDecimal fraction) {
        return fraction.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
