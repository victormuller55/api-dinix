package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.RecurringExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    Page<RecurringExpense> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<RecurringExpense> findByUserIdAndActiveTrue(UUID userId);

    Optional<RecurringExpense> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
