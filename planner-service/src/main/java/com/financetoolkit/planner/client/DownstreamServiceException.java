package com.financetoolkit.planner.client;

/**
 * Thrown when another service can't be reached or returns an error.
 *
 * In a monolith a method call can't "be down". Between microservices it can,
 * so the planner must expect it and report it clearly (HTTP 503) instead of
 * crashing with a confusing 500.
 */
public class DownstreamServiceException extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException(String serviceName, Throwable cause) {
        super(serviceName + " is unavailable: " + cause.getMessage(), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
