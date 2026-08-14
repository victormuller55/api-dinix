package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.CategoryKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        @Size(max = 80) String icon,
        CategoryKind kind,
        UUID parentCategoryId
) {
}
