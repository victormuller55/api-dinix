package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    @Query("""
            SELECT pi.product.id, pi.product.name,
                   SUM(pi.quantity), SUM(pi.totalPrice), AVG(pi.unitPrice), MAX(p.purchaseDate)
            FROM PurchaseItem pi
            JOIN pi.purchase p
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.purchaseDate BETWEEN :start AND :end
              AND pi.product IS NOT NULL
            GROUP BY pi.product.id, pi.product.name
            ORDER BY SUM(pi.totalPrice) DESC
            """)
    List<Object[]> summarizeProducts(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
