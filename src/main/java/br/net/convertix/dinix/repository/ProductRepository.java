package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByUserId(UUID userId, Pageable pageable);

    Optional<Product> findByIdAndUserId(UUID id, UUID userId);
}
