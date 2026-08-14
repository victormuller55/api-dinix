package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 255) String description,
        @Size(max = 120) String brand,
        UUID categoryId
) {
}
