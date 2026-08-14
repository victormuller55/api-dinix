package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        LocalDate transferDate,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
