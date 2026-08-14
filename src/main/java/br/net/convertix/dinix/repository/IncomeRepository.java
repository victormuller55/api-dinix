package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {

    Page<Income> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    Optional<Income> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    List<Income> findByUserIdAndActiveTrueAndRecurringTrue(UUID userId);

    Page<Income> findByUserIdAndActiveTrueAndReceivedDateBetween(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);
}
