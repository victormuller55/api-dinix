package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.FinancialAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialAlertRepository extends JpaRepository<FinancialAlert, UUID> {

    Page<FinancialAlert> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<FinancialAlert> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndReferenceKey(UUID userId, String referenceKey);
}
