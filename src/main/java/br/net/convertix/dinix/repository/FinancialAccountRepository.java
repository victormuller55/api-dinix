package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.FinancialAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {

    Page<FinancialAccount> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<FinancialAccount> findByUserIdAndActiveTrue(UUID userId);

    Optional<FinancialAccount> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
