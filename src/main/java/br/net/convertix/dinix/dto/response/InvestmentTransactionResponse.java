package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.InvestmentTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentTransactionResponse(
        UUID id,
        UUID investmentId,
        InvestmentTransactionType type,
        BigDecimal amount,
        BigDecimal quantity,
        BigDecimal price,
        UUID accountId,
        LocalDate transactionDate,
        String notes
) {
}
