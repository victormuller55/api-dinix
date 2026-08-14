package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Page<Transfer> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    Optional<Transfer> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    Page<Transfer> findByUserIdAndActiveTrueAndTransferDateBetween(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);
}
