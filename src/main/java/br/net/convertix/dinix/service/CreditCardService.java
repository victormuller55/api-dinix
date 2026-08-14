package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateCreditCardRequest;
import br.net.convertix.dinix.dto.request.UpdateCreditCardRequest;
import br.net.convertix.dinix.dto.response.CreditCardResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.CreditCardRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final InstallmentRepository installmentRepository;
    private final AuthService authService;
    private final AccountService accountService;

    public CreditCardService(
            CreditCardRepository creditCardRepository,
            InstallmentRepository installmentRepository,
            AuthService authService,
            AccountService accountService) {
        this.creditCardRepository = creditCardRepository;
        this.installmentRepository = installmentRepository;
        this.authService = authService;
        this.accountService = accountService;
    }

    @Transactional
    public CreditCardResponse create(UUID userId, CreateCreditCardRequest request) {
        User user = authService.getActive(userId);
        FinancialAccount account = request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null;
        CreditCard card = CreditCard.builder()
                .user(user)
                .account(account)
                .name(request.name())
                .bank(request.bank())
                .creditLimit(MoneyUtils.of(request.creditLimit()))
                .closingDay(request.closingDay())
                .dueDay(request.dueDay())
                .active(true)
                .build();
        return toResponse(creditCardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public PageResponse<CreditCardResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(creditCardRepository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<CreditCardResponse> listAll(UUID userId) {
        return creditCardRepository.findByUserIdAndActiveTrue(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CreditCardResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public CreditCardResponse update(UUID userId, UUID id, UpdateCreditCardRequest request) {
        CreditCard card = getOwned(userId, id);
        card.setAccount(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null);
        card.setName(request.name());
        card.setBank(request.bank());
        card.setCreditLimit(MoneyUtils.of(request.creditLimit()));
        card.setClosingDay(request.closingDay());
        card.setDueDay(request.dueDay());
        return toResponse(creditCardRepository.save(card));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        CreditCard card = getOwned(userId, id);
        card.setActive(false);
        creditCardRepository.save(card);
    }

    public CreditCard getOwned(UUID userId, UUID id) {
        return creditCardRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado"));
    }

    public CreditCard getOwnedOrNull(UUID userId, UUID id) {
        if (id == null) {
            return null;
        }
        return getOwned(userId, id);
    }

    public CreditCardResponse toResponse(CreditCard card) {
        BigDecimal used = MoneyUtils.of(installmentRepository.sumUsedLimit(
                card.getId(), List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE)));
        BigDecimal available = card.getCreditLimit().subtract(used);
        return new CreditCardResponse(
                card.getId(),
                card.getAccount() != null ? card.getAccount().getId() : null,
                card.getName(),
                card.getBank(),
                card.getCreditLimit(),
                used,
                available,
                card.getClosingDay(),
                card.getDueDay(),
                card.isActive(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
