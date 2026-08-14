package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record CategoryBreakdownResponse(
        String category,
        BigDecimal amount,
        BigDecimal percentage
) {
}
