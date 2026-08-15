package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.enums.RecurrenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringExpenseResponse(
        UUID id,
        String name,
        String description,
        BigDecimal amount,
        UUID categoryId,
        PaymentMethod paymentMethod,
        UUID accountId,
        UUID creditCardId,
        Integer dueDay,
        LocalDate startDate,
        LocalDate endDate,
        RecurrenceType recurrence,
        Integer lastPaidYear,
        Integer lastPaidMonth,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
