package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.CreditCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    Page<CreditCard> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);

    List<CreditCard> findByUserIdAndActiveTrue(UUID userId);

    Optional<CreditCard> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);
}
