package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Investment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {

    Page<Investment> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<Investment> findByUserIdAndActiveTrue(UUID userId);

    Optional<Investment> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
