package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCreditCardInvoiceRequest(
        @NotNull @DecimalMin("0.00") BigDecimal amount
) {
}
