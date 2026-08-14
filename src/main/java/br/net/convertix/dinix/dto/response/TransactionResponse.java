package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        LocalDate transactionDate,
        String description,
        UUID accountId,
        UUID creditCardId,
        UUID categoryId,
        UUID purchaseId,
        UUID installmentId,
        UUID incomeId,
        UUID transferId,
        boolean countsInMonthlyResult,
        boolean affectsAccountBalance,
        LocalDateTime createdAt
) {
}
