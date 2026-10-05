package com.mini.rate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

/** Exchange rates, hard-coded so the service needs no database. Each rate is "1 FROM = rate TO". */
@RestController
public class RateController {

    private static final Map<String, BigDecimal> RATES = Map.of(
            "USD-INR", new BigDecimal("83.50"),
            "EUR-INR", new BigDecimal("90.20"),
            "USD-EUR", new BigDecimal("0.92"),
            "GBP-INR", new BigDecimal("105.40"));

    public record Rate(String from, String to, BigDecimal rate) {
    }

    @GetMapping("/rates/{from}/{to}")
    public Rate getRate(@PathVariable String from, @PathVariable String to) {
        String f = from.toUpperCase();
        String t = to.toUpperCase();
        BigDecimal rate = RATES.get(f + "-" + t);
        if (rate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No rate for " + f + " -> " + t);
        }
        return new Rate(f, t, rate);
    }
}
