package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLocationRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 255) String description,
        UUID categoryId,
        @Size(max = 255) String address,
        @Size(max = 120) String city,
        @Size(max = 2) String state,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
