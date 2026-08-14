package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.InvestmentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {

    Page<InvestmentTransaction> findByInvestmentIdAndInvestmentUserId(
            UUID investmentId, UUID userId, Pageable pageable);

    List<InvestmentTransaction> findByInvestmentUserIdAndTransactionDateBetween(
            UUID userId, LocalDate start, LocalDate end);
}
