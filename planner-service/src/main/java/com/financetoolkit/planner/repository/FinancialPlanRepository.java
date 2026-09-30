package com.financetoolkit.planner.repository;

import com.financetoolkit.planner.model.FinancialPlan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data writes the SQL for us: save(), findById(), findAll() and more
 * come for free just by extending JpaRepository.
 */
public interface FinancialPlanRepository extends JpaRepository<FinancialPlan, Long> {
}
