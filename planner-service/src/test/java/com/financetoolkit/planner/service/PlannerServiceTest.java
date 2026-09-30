package com.financetoolkit.planner.service;

import com.financetoolkit.planner.client.GoalQuote;
import com.financetoolkit.planner.client.InvestmentServiceClient;
import com.financetoolkit.planner.client.LoanQuote;
import com.financetoolkit.planner.client.LoanServiceClient;
import com.financetoolkit.planner.dto.GoalDetails;
import com.financetoolkit.planner.dto.LoanDetails;
import com.financetoolkit.planner.dto.PlanRequest;
import com.financetoolkit.planner.model.FinancialPlan;
import com.financetoolkit.planner.model.PlanVerdict;
import com.financetoolkit.planner.repository.FinancialPlanRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests with Mockito "mocks": fake clients that return whatever we tell
 * them, so the budgeting rules can be tested without the other services running.
 */
class PlannerServiceTest {

    private final LoanServiceClient loanClient = mock(LoanServiceClient.class);
    private final InvestmentServiceClient investmentClient = mock(InvestmentServiceClient.class);
    private final FinancialPlanRepository repository = mock(FinancialPlanRepository.class);
    private final PlannerService service = new PlannerService(loanClient, investmentClient, repository);

    private static PlanRequest budget(int income, int expenses) {
        return new PlanRequest("test", BigDecimal.valueOf(income), BigDecimal.valueOf(expenses),
                new LoanDetails(BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(9), 10),
                new GoalDetails(BigDecimal.valueOf(500_000), BigDecimal.valueOf(12), 5));
    }

    private FinancialPlan assess(int income, int expenses, int emi, int sip) {
        return service.assess(budget(income, expenses), BigDecimal.valueOf(emi), BigDecimal.valueOf(sip));
    }

    @Test
    void comfortableBudgetIsAffordable() {
        FinancialPlan plan = assess(150_000, 60_000, 26_000, 9_000);
        assertThat(plan.getVerdict()).isEqualTo(PlanVerdict.AFFORDABLE);
        assertThat(plan.getMonthlyLeftover()).isEqualByComparingTo("55000");
        assertThat(plan.getEmiToIncomePercent()).isEqualByComparingTo("17.33");
    }

    @Test
    void negativeLeftoverIsNotAffordable() {
        FinancialPlan plan = assess(50_000, 30_000, 15_000, 10_000);
        assertThat(plan.getVerdict()).isEqualTo(PlanVerdict.NOT_AFFORDABLE);
        assertThat(plan.getAdvice()).contains("short by 5000");
    }

    @Test
    void emiAboveFortyPercentIsRiskyEvenWithMoneyLeft() {
        FinancialPlan plan = assess(100_000, 10_000, 45_000, 5_000);
        assertThat(plan.getMonthlyLeftover()).isPositive();
        assertThat(plan.getVerdict()).isEqualTo(PlanVerdict.RISKY);
    }

    @Test
    void smallBufferIsTight() {
        FinancialPlan plan = assess(100_000, 50_000, 30_000, 15_000);   // 5% left over
        assertThat(plan.getVerdict()).isEqualTo(PlanVerdict.TIGHT);
    }

    @Test
    void createPlanUsesBothServicesAndSaves() {
        when(loanClient.quote(any())).thenReturn(new LoanQuote(new BigDecimal("12667.58"), null));
        when(investmentClient.quote(any())).thenReturn(new GoalQuote(new BigDecimal("6061.69")));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        FinancialPlan plan = service.createPlan(budget(100_000, 40_000));

        assertThat(plan.getMonthlyEmi()).isEqualByComparingTo("12667.58");
        assertThat(plan.getMonthlyInvestment()).isEqualByComparingTo("6061.69");
        assertThat(plan.getMonthlyLeftover()).isEqualByComparingTo("41270.73");
    }
}
