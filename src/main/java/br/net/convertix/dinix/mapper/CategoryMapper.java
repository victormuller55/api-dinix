package br.net.convertix.dinix.mapper;

import br.net.convertix.dinix.dto.response.CategoryResponse;
import br.net.convertix.dinix.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIcon(),
                category.getKind(),
                category.getParentCategory() != null ? category.getParentCategory().getId() : null,
                category.isSystemDefault(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
