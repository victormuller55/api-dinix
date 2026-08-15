package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Page<Subscription> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<Subscription> findByUserIdAndActiveTrue(UUID userId);

    List<Subscription> findByActiveTrueAndNextBillingDateLessThanEqual(LocalDate date);

    Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

    Optional<Subscription> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
