package com.financetoolkit.loan.controller;

import com.financetoolkit.loan.dto.LoanRequest;
import com.financetoolkit.loan.dto.LoanSummary;
import com.financetoolkit.loan.dto.PrepaymentRequest;
import com.financetoolkit.loan.dto.PrepaymentResult;
import com.financetoolkit.loan.dto.ScheduleResponse;
import com.financetoolkit.loan.service.LoanCalculator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP layer only: turn JSON into Java objects, hand them to {@link LoanCalculator},
 * return the answer as JSON. No finance logic belongs here.
 *
 * These are POST (not GET) because the input is a JSON body, even though
 * nothing is saved — it's a calculation, not a state change.
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanCalculator calculator;

    // Constructor injection: Spring passes in the LoanCalculator bean for us.
    public LoanController(LoanCalculator calculator) {
        this.calculator = calculator;
    }

    /** EMI, total paid and total interest. {@code @Valid} rejects bad input with 400 before we get here. */
    @PostMapping("/emi")
    public LoanSummary emi(@Valid @RequestBody LoanRequest request) {
        return calculator.summarize(request);
    }

    /** Full month-by-month breakdown: where every rupee/dollar of each EMI goes. */
    @PostMapping("/schedule")
    public ScheduleResponse schedule(@Valid @RequestBody LoanRequest request) {
        var rows = calculator.schedule(request);
        return new ScheduleResponse(calculator.summarize(request), rows);
    }

    /** How much time and interest an extra monthly payment saves. */
    @PostMapping("/prepayment")
    public PrepaymentResult prepayment(@Valid @RequestBody PrepaymentRequest request) {
        return calculator.prepayment(request.loan(), request.extraMonthlyPayment());
    }
}
