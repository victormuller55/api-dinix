package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.PurchaseLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseLocationRepository extends JpaRepository<PurchaseLocation, UUID> {

    Page<PurchaseLocation> findByUserId(UUID userId, Pageable pageable);

    Optional<PurchaseLocation> findByIdAndUserId(UUID id, UUID userId);
}
