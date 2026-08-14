package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.CategoryKind;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        String icon,
        CategoryKind kind,
        UUID parentCategoryId,
        boolean systemDefault,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
