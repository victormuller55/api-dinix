package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NextPaymentItemResponse(
        UUID subscriptionId,
        String name,
        BigDecimal amount,
        LocalDate date
) {
}
