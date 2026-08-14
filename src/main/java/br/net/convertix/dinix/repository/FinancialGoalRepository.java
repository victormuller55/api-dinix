package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.FinancialGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {

    Page<FinancialGoal> findByUserId(UUID userId, Pageable pageable);

    Optional<FinancialGoal> findByIdAndUserId(UUID id, UUID userId);
}
