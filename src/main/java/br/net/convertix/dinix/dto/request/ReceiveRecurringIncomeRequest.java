package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceiveRecurringIncomeRequest(
        @NotNull UUID accountId
) {
}
