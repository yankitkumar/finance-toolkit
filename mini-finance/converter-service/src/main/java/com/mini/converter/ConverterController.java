package com.mini.converter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
public class ConverterController {

    public record Conversion(String from, String to, BigDecimal amount, BigDecimal rate, BigDecimal result) {
    }

    private final RateClient rateClient;

    public ConverterController(RateClient rateClient) {
        this.rateClient = rateClient;
    }

    /** Example: GET /convert?from=USD&to=INR&amount=100 */
    @GetMapping("/convert")
    public Conversion convert(@RequestParam String from,
                              @RequestParam String to,
                              @RequestParam BigDecimal amount) {
        BigDecimal rate = rateClient.getRate(from.toUpperCase(), to.toUpperCase());
        BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new Conversion(from.toUpperCase(), to.toUpperCase(), amount, rate, result);
    }
}
