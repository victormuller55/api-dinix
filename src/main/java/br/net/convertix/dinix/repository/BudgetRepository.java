package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdAndMonthAndYear(UUID userId, Integer month, Integer year);

    Page<Budget> findByUserIdAndMonthAndYear(UUID userId, Integer month, Integer year, Pageable pageable);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, Integer month, Integer year);
}
