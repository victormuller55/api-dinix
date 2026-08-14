package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String brand,
        UUID categoryId,
        BigDecimal averagePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
