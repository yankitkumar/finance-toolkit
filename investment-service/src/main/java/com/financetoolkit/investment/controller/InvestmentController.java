package com.financetoolkit.investment.controller;

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
import com.financetoolkit.investment.service.InvestmentCalculator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * One endpoint per question an investor asks. The controller only maps HTTP
 * to method calls; the math is in {@link InvestmentCalculator}.
 */
@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentCalculator calculator;

    public InvestmentController(InvestmentCalculator calculator) {
        this.calculator = calculator;
    }

    /** "If I invest this once, what will it become?" */
    @PostMapping("/lump-sum")
    public LumpSumResult lumpSum(@Valid @RequestBody LumpSumRequest request) {
        return calculator.lumpSum(request);
    }

    /** "If I invest this every month, what will it become?" */
    @PostMapping("/sip")
    public SipResult sip(@Valid @RequestBody SipRequest request) {
        return calculator.sip(request);
    }

    /** "How much must I invest monthly to reach my goal?" (planner-service calls this one) */
    @PostMapping("/goal")
    public GoalResult goal(@Valid @RequestBody GoalRequest request) {
        return calculator.goal(request);
    }

    /** "How much richer am I really, after inflation?" */
    @PostMapping("/real-return")
    public RealReturnResult realReturn(@Valid @RequestBody RealReturnRequest request) {
        return calculator.realReturn(request);
    }

    /** "What steady yearly rate did my investment actually grow at?" */
    @PostMapping("/cagr")
    public CagrResult cagr(@Valid @RequestBody CagrRequest request) {
        return calculator.cagr(request);
    }

    /** "Is this project worth investing in?" (NPV + IRR) */
    @PostMapping("/evaluate-project")
    public ProjectEvaluation evaluateProject(@Valid @RequestBody ProjectRequest request) {
        return calculator.evaluateProject(request);
    }
}
