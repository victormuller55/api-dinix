package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.RecurringIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringIncomeRepository extends JpaRepository<RecurringIncome, UUID> {

    Page<RecurringIncome> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<RecurringIncome> findByUserIdAndActiveTrue(UUID userId);

    Optional<RecurringIncome> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
