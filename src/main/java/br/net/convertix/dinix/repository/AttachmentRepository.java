package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTransactionIdAndTransactionUserId(UUID transactionId, UUID userId);

    Page<Attachment> findByTransactionIdAndTransactionUserId(UUID transactionId, UUID userId, Pageable pageable);

    Optional<Attachment> findByIdAndTransactionUserId(UUID id, UUID userId);
}
