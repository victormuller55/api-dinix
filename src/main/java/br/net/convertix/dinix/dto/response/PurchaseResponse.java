package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record PurchaseResponse(
        UUID id,
        String description,
        LocalDate purchaseDate,
        LocalTime purchaseTime,
        BigDecimal totalAmount,
        UUID categoryId,
        UUID locationId,
        PaymentMethod paymentMethod,
        UUID financialAccountId,
        UUID creditCardId,
        String notes,
        Integer numberOfInstallments,
        BigDecimal installmentAmount,
        LocalDate firstInstallmentDate,
        List<PurchaseItemResponse> items,
        List<InstallmentResponse> installments,
        List<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
