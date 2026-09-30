package com.financetoolkit.investment.service;

import com.financetoolkit.investment.dto.CagrRequest;
import com.financetoolkit.investment.dto.GoalRequest;
import com.financetoolkit.investment.dto.GoalResult;
import com.financetoolkit.investment.dto.LumpSumRequest;
import com.financetoolkit.investment.dto.ProjectEvaluation;
import com.financetoolkit.investment.dto.ProjectRequest;
import com.financetoolkit.investment.dto.RealReturnRequest;
import com.financetoolkit.investment.dto.RealReturnResult;
import com.financetoolkit.investment.dto.SipRequest;
import com.financetoolkit.investment.dto.SipResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/** Each expected value can be checked by hand or with any online calculator. */
class InvestmentCalculatorTest {

    private final InvestmentCalculator calculator = new InvestmentCalculator();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static List<BigDecimal> flows(int... values) {
        return Arrays.stream(values).mapToObj(BigDecimal::valueOf).toList();
    }

    @Test
    void lumpSumCompoundsYearly() {
        // 1,000 × 1.05^10 = 1,628.89
        var result = calculator.lumpSum(new LumpSumRequest(bd("1000"), bd("5"), 10, 1));
        assertThat(result.futureValue()).isEqualByComparingTo("1628.89");
        assertThat(result.interestEarned()).isEqualByComparingTo("628.89");
    }

    @Test
    void monthlyCompoundingEarnsMoreThanYearly() {
        var yearly = calculator.lumpSum(new LumpSumRequest(bd("1000"), bd("12"), 1, 1));
        var monthly = calculator.lumpSum(new LumpSumRequest(bd("1000"), bd("12"), 1, 12));
        assertThat(yearly.futureValue()).isEqualByComparingTo("1120.00");
        assertThat(monthly.futureValue()).isEqualByComparingTo("1126.83");
    }

    @Test
    void sipMatchesStandardSipCalculators() {
        // 10,000 a month at 12% for 10 years → about 23.23 lakh (12 lakh invested).
        SipResult result = calculator.sip(new SipRequest(bd("10000"), bd("12"), 10));
        assertThat(result.totalInvested()).isEqualByComparingTo("1200000.00");
        assertThat(result.futureValue()).isEqualByComparingTo("2323390.76");
        assertThat(result.wealthGained()).isEqualByComparingTo("1123390.76");
    }

    @Test
    void sipWithZeroReturnIsJustTheDeposits() {
        SipResult result = calculator.sip(new SipRequest(bd("500"), bd("0"), 2));
        assertThat(result.futureValue()).isEqualByComparingTo("12000.00");
        assertThat(result.wealthGained()).isEqualByComparingTo("0");
    }

    @Test
    void goalAmountIsEnoughToReachTheTarget() {
        // Feeding the goal's monthly amount back into the SIP formula must reach the target.
        GoalResult goal = calculator.goal(new GoalRequest(bd("1000000"), bd("12"), 5));
        SipResult check = calculator.sip(new SipRequest(goal.monthlyInvestment(), bd("12"), 5));

        assertThat(check.futureValue()).isGreaterThanOrEqualTo(bd("1000000"));
        // ...and not by more than one extra cent per month would add.
        assertThat(check.futureValue()).isLessThan(bd("1000001"));
    }

    @Test
    void goalWithZeroReturnNeverShowsNegativeGrowth() {
        // 1,000 over 12 months = 83.33.. → rounded up to 83.34, so 1,000.08 is invested.
        GoalResult goal = calculator.goal(new GoalRequest(bd("1000"), bd("0"), 1));
        assertThat(goal.monthlyInvestment()).isEqualByComparingTo("83.34");
        assertThat(goal.growthContribution()).isEqualByComparingTo("0");
    }

    @Test
    void realReturnIsLessThanTheShortcut() {
        RealReturnResult result = calculator.realReturn(new RealReturnRequest(bd("7"), bd("5")));
        assertThat(result.realRatePercent()).isEqualByComparingTo("1.90");
        assertThat(result.shortcutRatePercent()).isEqualByComparingTo("2.00");
    }

    @Test
    void cagrOfADoubleInFiveYears() {
        var result = calculator.cagr(new CagrRequest(bd("100"), bd("200"), 5));
        assertThat(result.cagrPercent()).isEqualByComparingTo("14.87");
    }

    @Test
    void npvAndIrrOfASimpleProject() {
        // -1000 today, +500 for 3 years. NPV at 10% = 243.43, IRR = 23.38%.
        ProjectEvaluation result = calculator.evaluateProject(
                new ProjectRequest(bd("10"), flows(-1000, 500, 500, 500)));
        assertThat(result.npv()).isEqualByComparingTo("243.43");
        assertThat(result.irrPercent()).isEqualByComparingTo("23.38");
        assertThat(result.decision()).isEqualTo("INVEST");
    }

    @Test
    void npvIsZeroAtTheIrr() {
        List<BigDecimal> cashFlows = flows(-100000, 30000, 30000, 30000, 30000, 30000);
        double irr = calculator.irr(cashFlows);
        double npvAtIrr = calculator.npv(BigDecimal.valueOf(irr), cashFlows).doubleValue();
        assertThat(npvAtIrr).isCloseTo(0.0, within(0.01));
    }

    @Test
    void rejectsProjectThatEarnsLessThanRequired() {
        ProjectEvaluation result = calculator.evaluateProject(
                new ProjectRequest(bd("20"), flows(-1000, 300, 300, 300, 300)));
        assertThat(result.npv()).isNegative();
        assertThat(result.decision()).isEqualTo("REJECT");
    }

    @Test
    void irrNeedsMoneyOutAndMoneyIn() {
        assertThatThrownBy(() -> calculator.evaluateProject(new ProjectRequest(bd("10"), flows(100, 200, 300))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outflow");
    }
}
