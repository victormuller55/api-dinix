package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    Optional<Installment> findByIdAndPurchaseUserId(UUID id, UUID userId);

    List<Installment> findByPurchaseUserIdAndDueDateBetweenAndStatusIn(
            UUID userId, LocalDate start, LocalDate end, Collection<InstallmentStatus> statuses);

    List<Installment> findByPurchaseCreditCardIdAndStatusIn(UUID creditCardId, Collection<InstallmentStatus> statuses);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM Installment i
            WHERE i.purchase.creditCard.id = :creditCardId
              AND i.purchase.active = true
              AND i.status IN :statuses
            """)
    BigDecimal sumUsedLimit(
            @Param("creditCardId") UUID creditCardId,
            @Param("statuses") Collection<InstallmentStatus> statuses);

    List<Installment> findByStatusAndDueDateBefore(InstallmentStatus status, LocalDate date);
}
