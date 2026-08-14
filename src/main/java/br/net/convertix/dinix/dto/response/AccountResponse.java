package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        String bankName,
        AccountType accountType,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        String color,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
