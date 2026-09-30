package com.financetoolkit.loan.dto;

import java.util.List;

/** The loan's headline numbers plus its full month-by-month breakdown. */
public record ScheduleResponse(LoanSummary summary, List<AmortizationRow> schedule) {
}
