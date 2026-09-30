package com.financetoolkit.planner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A saved plan: one row in the planner's own "financial_plans" table.
 *
 * It stores the *results* (EMI, SIP, verdict), not just the inputs, so a plan
 * keeps showing what was true when it was made even if rates change later.
 *
 * For simplicity the controller returns this entity directly as JSON. In a
 * larger app you'd usually map it to a separate response record.
 */
@Entity
@Table(name = "financial_plans")
public class FinancialPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;

    /** From loan-service. */
    private BigDecimal monthlyEmi;

    /** From investment-service: the monthly SIP needed to reach the goal. */
    private BigDecimal monthlyInvestment;

    /** income − expenses − EMI − investment. Negative means the plan doesn't fit. */
    private BigDecimal monthlyLeftover;

    /** EMI as a percentage of income — the number lenders look at first. */
    private BigDecimal emiToIncomePercent;

    @Enumerated(EnumType.STRING)   // store "AFFORDABLE", not 0, so the DB stays readable
    @Column(nullable = false, length = 20)
    private PlanVerdict verdict;

    @Column(length = 500)
    private String advice;

    private Instant createdAt;

    /** JPA needs a no-argument constructor to build objects from database rows. */
    protected FinancialPlan() {
    }

    public FinancialPlan(String name, BigDecimal monthlyIncome, BigDecimal monthlyExpenses,
                         BigDecimal monthlyEmi, BigDecimal monthlyInvestment, BigDecimal monthlyLeftover,
                         BigDecimal emiToIncomePercent, PlanVerdict verdict, String advice) {
        this.name = name;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpenses = monthlyExpenses;
        this.monthlyEmi = monthlyEmi;
        this.monthlyInvestment = monthlyInvestment;
        this.monthlyLeftover = monthlyLeftover;
        this.emiToIncomePercent = emiToIncomePercent;
        this.verdict = verdict;
        this.advice = advice;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public BigDecimal getMonthlyInvestment() { return monthlyInvestment; }
    public BigDecimal getMonthlyLeftover() { return monthlyLeftover; }
    public BigDecimal getEmiToIncomePercent() { return emiToIncomePercent; }
    public PlanVerdict getVerdict() { return verdict; }
    public String getAdvice() { return advice; }
    public Instant getCreatedAt() { return createdAt; }
}
