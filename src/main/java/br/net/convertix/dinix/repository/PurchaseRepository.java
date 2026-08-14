package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Page<Purchase> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    Optional<Purchase> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    @Query("""
            SELECT p FROM Purchase p
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.purchaseDate BETWEEN :start AND :end
            """)
    Page<Purchase> findByUserAndPeriod(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query("""
            SELECT p FROM Purchase p
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.purchaseDate IN :dates
            """)
    Page<Purchase> findByUserAndDates(
            @Param("userId") UUID userId,
            @Param("dates") Collection<LocalDate> dates,
            Pageable pageable);
}
