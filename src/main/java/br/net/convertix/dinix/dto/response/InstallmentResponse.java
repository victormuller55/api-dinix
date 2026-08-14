package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InstallmentResponse(
        UUID id,
        Integer installmentNumber,
        Integer totalInstallments,
        BigDecimal amount,
        LocalDate dueDate,
        InstallmentStatus status,
        LocalDateTime paidAt
) {
}
