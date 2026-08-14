package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 120) String bankName,
        @NotNull AccountType accountType,
        @NotNull BigDecimal currentBalance
) {
}
