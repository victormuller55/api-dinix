package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductStatResponse(
        UUID productId,
        String name,
        BigDecimal quantity,
        BigDecimal totalSpent,
        BigDecimal averagePrice,
        LocalDate lastPurchaseDate
) {
}
