package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateCategoryRequest;
import br.net.convertix.dinix.dto.request.UpdateCategoryRequest;
import br.net.convertix.dinix.dto.response.CategoryResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.CategoryKind;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.mapper.CategoryMapper;
import br.net.convertix.dinix.repository.CategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuthService authService;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            AuthService authService,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.authService = authService;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        User user = authService.getActive(userId);
        Category parent = request.parentCategoryId() != null ? getOwned(userId, request.parentCategoryId()) : null;
        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .icon(request.icon())
                .kind(request.kind() != null ? request.kind() : CategoryKind.BOTH)
                .parentCategory(parent)
                .systemDefault(false)
                .active(true)
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(
                categoryRepository.findByUserIdAndActiveTrueOrderByNameAsc(userId, pageable)
                        .map(categoryMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID userId, UUID id) {
        return categoryMapper.toResponse(getOwned(userId, id));
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID id, UpdateCategoryRequest request) {
        Category category = getOwned(userId, id);
        if (request.parentCategoryId() != null && request.parentCategoryId().equals(id)) {
            throw new BusinessException("Categoria não pode ser pai de si mesma");
        }
        Category parent = request.parentCategoryId() != null ? getOwned(userId, request.parentCategoryId()) : null;
        category.setName(request.name());
        category.setDescription(request.description());
        category.setIcon(request.icon());
        category.setKind(request.kind() != null ? request.kind() : category.getKind());
        category.setParentCategory(parent);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Category category = getOwned(userId, id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    public Category getOwned(UUID userId, UUID id) {
        return categoryRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    public Category getOwnedOrNull(UUID userId, UUID id) {
        if (id == null) {
            return null;
        }
        return getOwned(userId, id);
    }
}
