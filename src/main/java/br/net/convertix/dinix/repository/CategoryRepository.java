package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserIdAndActiveTrueOrderByNameAsc(UUID userId);

    Page<Category> findByUserIdAndActiveTrueOrderByNameAsc(UUID userId, Pageable pageable);

    Optional<Category> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndParentCategoryIdAndActiveTrue(
            UUID userId, String name, UUID parentCategoryId);
}
