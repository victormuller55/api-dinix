package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.enums.RecurrenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        String name,
        String description,
        BigDecimal amount,
        UUID categoryId,
        PaymentMethod paymentMethod,
        UUID accountId,
        UUID creditCardId,
        Integer billingDay,
        LocalDate startDate,
        LocalDate nextBillingDate,
        Integer lastPaidYear,
        Integer lastPaidMonth,
        RecurrenceType recurrence,
        boolean active,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
