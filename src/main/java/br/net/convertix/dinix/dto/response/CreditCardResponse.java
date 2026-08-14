package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardResponse(
        UUID id,
        UUID accountId,
        String name,
        String bank,
        BigDecimal creditLimit,
        BigDecimal usedLimit,
        BigDecimal availableLimit,
        Integer closingDay,
        Integer dueDay,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
