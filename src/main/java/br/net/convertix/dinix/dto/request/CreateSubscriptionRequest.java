package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.enums.RecurrenceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSubscriptionRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 255) String description,
        @NotNull @Positive BigDecimal amount,
        UUID categoryId,
        @NotNull PaymentMethod paymentMethod,
        UUID accountId,
        UUID creditCardId,
        @NotNull @Min(1) @Max(31) Integer billingDay,
        @NotNull LocalDate startDate,
        RecurrenceType recurrence,
        Boolean chargeToday
) {
}
