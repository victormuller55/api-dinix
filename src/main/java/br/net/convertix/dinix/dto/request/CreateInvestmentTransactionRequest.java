package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.InvestmentTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateInvestmentTransactionRequest(
        @NotNull InvestmentTransactionType type,
        @NotNull @Positive BigDecimal amount,
        BigDecimal quantity,
        BigDecimal price,
        UUID accountId,
        @NotNull LocalDate transactionDate,
        String notes
) {
}
