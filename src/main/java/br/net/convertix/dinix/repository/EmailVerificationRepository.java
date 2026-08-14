package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Optional<EmailVerification> findTopByEmailIgnoreCaseAndVerifiedTrueOrderByVerifiedAtDesc(String email);
}
