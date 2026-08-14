package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateIncomeRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull @Positive BigDecimal amount,
        UUID categoryId,
        @NotNull UUID accountId,
        @NotNull LocalDate receivedDate,
        boolean recurring,
        @Size(max = 1000) String notes
) {
}
