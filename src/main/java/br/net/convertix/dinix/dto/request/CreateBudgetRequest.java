package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotNull UUID categoryId,
        @NotNull @Positive BigDecimal amountLimit,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull Integer year
) {
}
