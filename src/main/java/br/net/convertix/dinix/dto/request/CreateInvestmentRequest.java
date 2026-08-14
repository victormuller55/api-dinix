package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.InvestmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvestmentRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 120) String institution,
        @NotNull InvestmentType type,
        @Size(max = 30) String ticker
) {
}
