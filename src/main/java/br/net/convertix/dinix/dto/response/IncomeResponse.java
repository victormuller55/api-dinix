package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record IncomeResponse(
        UUID id,
        String description,
        BigDecimal amount,
        UUID categoryId,
        UUID accountId,
        LocalDate receivedDate,
        boolean recurring,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
