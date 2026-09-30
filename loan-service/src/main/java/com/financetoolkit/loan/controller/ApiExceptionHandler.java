package com.financetoolkit.loan.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.TreeMap;

/**
 * Turns validation failures into a clear 400 response instead of a stack trace.
 *
 * Response shape (RFC 7807 "problem details", built into Spring):
 * {"title": "Invalid request", "status": 400, "errors": {"years": "must be greater than or equal to 1"}}
 *
 * Each microservice keeps its own copy of this small class on purpose: services
 * should be deployable on their own, without a shared library tying them together.
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
}
