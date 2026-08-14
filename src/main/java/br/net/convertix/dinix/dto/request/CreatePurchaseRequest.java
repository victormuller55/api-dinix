package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull LocalDate purchaseDate,
        LocalTime purchaseTime,
        @NotNull @Positive BigDecimal totalAmount,
        UUID categoryId,
        UUID locationId,
        @NotNull PaymentMethod paymentMethod,
        UUID financialAccountId,
        UUID creditCardId,
        @Size(max = 1000) String notes,
        Integer numberOfInstallments,
        LocalDate firstInstallmentDate,
        List<UUID> tagIds,
        @Valid List<CreatePurchaseItemRequest> items
) {
}
