package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PayRecurringExpenseRequest(
        @NotNull PaymentMethod paymentMethod,
        UUID financialAccountId,
        UUID creditCardId
) {
}
