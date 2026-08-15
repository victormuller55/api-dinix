package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.CreditCardInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardInvoiceResponse(
        UUID id,
        UUID creditCardId,
        Integer year,
        Integer month,
        BigDecimal amount,
        CreditCardInvoiceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
