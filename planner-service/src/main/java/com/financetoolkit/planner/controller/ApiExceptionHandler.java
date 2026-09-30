package com.financetoolkit.planner.controller;

import com.financetoolkit.planner.client.DownstreamServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Maps errors to clear HTTP responses:
 *   bad input                  → 400 with the list of bad fields
 *   unknown plan id            → 404
 *   another service is down    → 503 naming that service
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new TreeMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleStatus(ResponseStatusException ex) {
        return ex.getBody();
    }

    /**
     * 503 Service Unavailable (not 500) tells the caller "the planner is fine,
     * but something it depends on isn't — try again later".
     */
    @ExceptionHandler(DownstreamServiceException.class)
    public ProblemDetail handleDownstream(DownstreamServiceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Could not get an answer from " + ex.getServiceName() + ". Is it running?");
        problem.setTitle("Dependent service unavailable");
        problem.setProperty("service", ex.getServiceName());
        return problem;
    }
}
