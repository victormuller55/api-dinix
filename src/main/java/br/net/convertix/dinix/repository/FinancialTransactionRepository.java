package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.FinancialTransaction;
import br.net.convertix.dinix.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID>,
        JpaSpecificationExecutor<FinancialTransaction> {

    Optional<FinancialTransaction> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    List<FinancialTransaction> findByInstallmentIdAndActiveTrue(UUID installmentId);

    List<FinancialTransaction> findByPurchaseIdAndActiveTrue(UUID purchaseId);

    List<FinancialTransaction> findByIncomeIdAndActiveTrue(UUID incomeId);

    List<FinancialTransaction> findByTransferIdAndActiveTrue(UUID transferId);

    List<FinancialTransaction> findByInvestmentTransactionIdAndActiveTrue(UUID investmentTransactionId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM FinancialTransaction t
            WHERE t.user.id = :userId
              AND t.active = true
              AND t.countsInMonthlyResult = true
              AND t.type = :type
              AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumByTypeInPeriod(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            SELECT COUNT(t)
            FROM FinancialTransaction t
            WHERE t.user.id = :userId
              AND t.active = true
              AND t.countsInMonthlyResult = true
              AND t.type = :type
              AND t.transactionDate BETWEEN :start AND :end
            """)
    long countByTypeInPeriod(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(c.name, 'Sem categoria'), COALESCE(SUM(t.amount), 0)
            FROM FinancialTransaction t
            LEFT JOIN t.category c
            WHERE t.user.id = :userId
              AND t.active = true
              AND t.countsInMonthlyResult = true
              AND t.type = br.net.convertix.dinix.enums.TransactionType.EXPENSE
              AND t.transactionDate BETWEEN :start AND :end
            GROUP BY c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> sumExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM FinancialTransaction t
            WHERE t.user.id = :userId
              AND t.active = true
              AND t.countsInMonthlyResult = true
              AND t.type = br.net.convertix.dinix.enums.TransactionType.EXPENSE
              AND t.category.id = :categoryId
              AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumExpensesByCategoryId(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(loc.name, 'Sem local'), COALESCE(SUM(p.totalAmount), 0)
            FROM Purchase p
            LEFT JOIN p.location loc
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.purchaseDate BETWEEN :start AND :end
            GROUP BY loc.name
            ORDER BY SUM(p.totalAmount) DESC
            """)
    List<Object[]> topLocations(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    Page<FinancialTransaction> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);
}
