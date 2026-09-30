package com.financetoolkit.planner.controller;

import com.financetoolkit.planner.dto.PlanRequest;
import com.financetoolkit.planner.model.FinancialPlan;
import com.financetoolkit.planner.service.PlannerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

/**
 * Unlike the calculator services, this one creates and stores data, so it
 * follows the usual REST pattern for a "resource":
 *   POST /api/plans       create  → 201 Created + Location header
 *   GET  /api/plans/{id}  read one
 *   GET  /api/plans       read all
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlannerService plannerService;

    public PlanController(PlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @PostMapping
    public ResponseEntity<FinancialPlan> create(@Valid @RequestBody PlanRequest request) {
        FinancialPlan plan = plannerService.createPlan(request);
        // 201 + "Location: /api/plans/7" tells the client where the new plan lives.
        return ResponseEntity.created(URI.create("/api/plans/" + plan.getId())).body(plan);
    }

    @GetMapping("/{id}")
    public FinancialPlan get(@PathVariable Long id) {
        return plannerService.findPlan(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No plan with id " + id));
    }

    @GetMapping
    public List<FinancialPlan> list() {
        return plannerService.allPlans();
    }
}
