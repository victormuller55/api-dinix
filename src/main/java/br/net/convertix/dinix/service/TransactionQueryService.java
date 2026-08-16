package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateAttachmentRequest;
import br.net.convertix.dinix.dto.request.CreateTagRequest;
import br.net.convertix.dinix.dto.response.AttachmentResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.TagResponse;
import br.net.convertix.dinix.dto.response.TransactionResponse;
import br.net.convertix.dinix.entity.Attachment;
import br.net.convertix.dinix.entity.FinancialTransaction;
import br.net.convertix.dinix.entity.Tag;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.exception.ConflictException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.mapper.TransactionMapper;
import br.net.convertix.dinix.repository.AttachmentRepository;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.TagRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionQueryService {

    private final FinancialTransactionRepository transactionRepository;
    private final TagRepository tagRepository;
    private final AttachmentRepository attachmentRepository;
    private final AuthService authService;
    private final TransactionMapper transactionMapper;

    public TransactionQueryService(
            FinancialTransactionRepository transactionRepository,
            TagRepository tagRepository,
            AttachmentRepository attachmentRepository,
            AuthService authService,
            TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.tagRepository = tagRepository;
        this.attachmentRepository = attachmentRepository;
        this.authService = authService;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> search(
            UUID userId,
            String q,
            TransactionType type,
            UUID categoryId,
            UUID accountId,
            UUID creditCardId,
            LocalDate start,
            LocalDate end,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Boolean affectsAccountBalance,
            Pageable pageable) {
        Specification<FinancialTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.isTrue(root.get("active")));
            if (q != null && !q.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + q.toLowerCase() + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }
            if (creditCardId != null) {
                predicates.add(cb.equal(root.get("creditCard").get("id"), creditCardId));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), end));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (affectsAccountBalance != null) {
                predicates.add(cb.equal(root.get("affectsAccountBalance"), affectsAccountBalance));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt")));
        return PageResponse.from(transactionRepository.findAll(spec, ordenado).map(transactionMapper::toResponse));
    }

    @Transactional
    public TagResponse createTag(UUID userId, CreateTagRequest request) {
        User user = authService.getActive(userId);
        String name = request.name().startsWith("#") ? request.name() : "#" + request.name();
        tagRepository.findByUserIdAndNameIgnoreCase(userId, name).ifPresent(existing -> {
            throw new ConflictException("Tag já existe");
        });
        Tag tag = tagRepository.save(Tag.builder().user(user).name(name).build());
        return new TagResponse(tag.getId(), tag.getName());
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> listTags(UUID userId, Pageable pageable) {
        return PageResponse.from(
                tagRepository.findByUserIdOrderByNameAsc(userId, pageable)
                        .map(tag -> new TagResponse(tag.getId(), tag.getName())));
    }

    @Transactional
    public AttachmentResponse createAttachment(UUID userId, CreateAttachmentRequest request) {
        FinancialTransaction transaction = transactionRepository.findByIdAndUserIdAndActiveTrue(request.transactionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
        Attachment attachment = attachmentRepository.save(Attachment.builder()
                .transaction(transaction)
                .fileName(request.fileName())
                .fileUrl(request.fileUrl())
                .contentType(request.contentType())
                .build());
        return toAttachment(attachment);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttachmentResponse> listAttachments(UUID userId, UUID transactionId, Pageable pageable) {
        return PageResponse.from(
                attachmentRepository.findByTransactionIdAndTransactionUserId(transactionId, userId, pageable)
                        .map(this::toAttachment));
    }

    private AttachmentResponse toAttachment(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(), attachment.getTransaction().getId(), attachment.getFileName(),
                attachment.getFileUrl(), attachment.getContentType(), attachment.getCreatedAt());
    }
}
