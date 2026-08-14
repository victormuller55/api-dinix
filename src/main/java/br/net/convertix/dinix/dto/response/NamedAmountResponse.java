package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record NamedAmountResponse(
        String name,
        BigDecimal amount
) {
}
