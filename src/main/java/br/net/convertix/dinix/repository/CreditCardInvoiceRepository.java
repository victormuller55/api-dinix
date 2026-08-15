package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.CreditCardInvoice;
import br.net.convertix.dinix.enums.CreditCardInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {

    List<CreditCardInvoice> findByCreditCardIdOrderByReferenceYearAscReferenceMonthAsc(UUID creditCardId);

    Optional<CreditCardInvoice> findByIdAndCreditCardUserId(UUID id, UUID userId);

    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceYearAndReferenceMonth(
            UUID creditCardId, Integer referenceYear, Integer referenceMonth);

    Optional<CreditCardInvoice> findByCreditCardIdAndStatus(UUID creditCardId, CreditCardInvoiceStatus status);

    List<CreditCardInvoice> findByCreditCardIdAndStatusOrderByReferenceYearAscReferenceMonthAsc(
            UUID creditCardId, CreditCardInvoiceStatus status);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM CreditCardInvoice i
            WHERE i.creditCard.id = :creditCardId
              AND i.status IN :statuses
            """)
    BigDecimal sumAmountByCardAndStatuses(
            @Param("creditCardId") UUID creditCardId,
            @Param("statuses") Collection<CreditCardInvoiceStatus> statuses);
}
