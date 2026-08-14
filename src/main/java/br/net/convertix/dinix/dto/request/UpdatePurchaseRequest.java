package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record UpdatePurchaseRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull LocalDate purchaseDate,
        LocalTime purchaseTime,
        UUID categoryId,
        UUID locationId,
        PaymentMethod paymentMethod,
        @Size(max = 1000) String notes,
        List<UUID> tagIds
) {
}
