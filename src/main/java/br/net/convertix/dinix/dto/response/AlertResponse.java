package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.AlertType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        AlertType type,
        String title,
        String message,
        BigDecimal amount,
        LocalDate dueDate,
        boolean read,
        LocalDateTime createdAt
) {
}
