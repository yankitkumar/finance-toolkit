package com.financetoolkit.gateway;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * When a service behind the gateway is down, answer 503 and name the service,
 * instead of Spring's generic "500 Internal Server Error".
 *
 * 500 means "the gateway itself broke"; 503 means "the gateway is fine, but
 * the service it forwards to isn't reachable right now" — which is the truth,
 * and tells the caller that retrying later may work.
 *
 * The gateway is built on Spring WebFlux (non-blocking), so errors are handled
 * with a WebFlux ErrorWebExceptionHandler and the response is written as a Mono.
 */
@Component
@Order(-2) // run before Spring Boot's default error handler, which has order -1
public class ServiceUnavailableHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (!isUnreachable(ex) || response.isCommitted()) {
            return Mono.error(ex); // not a "service is down" error: let the default handler deal with it
        }

        // The gateway remembers which route matched; its id is the service name from application.yml.
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String service = route != null ? route.getId() : "target service";

        String body = """
                {"title":"Service unavailable","status":503,"detail":"%s is not reachable. Is it running?","service":"%s"}"""
                .formatted(service, service);

        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * True if the error (or anything that caused it) means "couldn't reach the service":
     *   ConnectException     — the host exists but nothing is listening on the port
     *   UnknownHostException — the host name doesn't resolve; in Docker Compose this is
     *                          what happens when a service's container isn't running
     */
    private static boolean isUnreachable(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof ConnectException || t instanceof UnknownHostException) {
                return true;
            }
        }
        return false;
    }
}
