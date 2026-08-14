package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateProductRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.ProductResponse;
import br.net.convertix.dinix.entity.Product;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuthService authService;
    private final CategoryService categoryService;

    public ProductService(
            ProductRepository productRepository,
            AuthService authService,
            CategoryService categoryService) {
        this.productRepository = productRepository;
        this.authService = authService;
        this.categoryService = categoryService;
    }

    @Transactional
    public ProductResponse create(UUID userId, CreateProductRequest request) {
        User user = authService.getActive(userId);
        Product product = Product.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .brand(request.brand())
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(productRepository.findByUserId(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public ProductResponse update(UUID userId, UUID id, CreateProductRequest request) {
        Product product = getOwned(userId, id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBrand(request.brand());
        product.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        productRepository.delete(getOwned(userId, id));
    }

    public Product getOwned(UUID userId, UUID id) {
        return productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(), product.getName(), product.getDescription(), product.getBrand(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getAveragePrice(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
