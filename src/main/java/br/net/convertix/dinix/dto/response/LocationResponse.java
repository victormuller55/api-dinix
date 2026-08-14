package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        String address,
        String city,
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
