package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateCreditCardRequest;
import br.net.convertix.dinix.dto.request.UpdateCreditCardRequest;
import br.net.convertix.dinix.dto.response.CreditCardResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.CreditCardInvoice;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.CreditCardInvoiceStatus;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.CreditCardInvoiceRepository;
import br.net.convertix.dinix.repository.CreditCardRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    private static final EnumSet<CreditCardInvoiceStatus> USED_LIMIT_STATUSES = EnumSet.of(
            CreditCardInvoiceStatus.CURRENT,
            CreditCardInvoiceStatus.UPCOMING,
            CreditCardInvoiceStatus.CLOSED
    );

    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;
    private final AuthService authService;
    private final AccountService accountService;

    public CreditCardService(
            CreditCardRepository creditCardRepository,
            CreditCardInvoiceRepository invoiceRepository,
            AuthService authService,
            AccountService accountService) {
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
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
        CreditCard saved = creditCardRepository.save(card);
        ensureCurrentInvoice(saved);
        return toResponse(saved);
    }

    @Transactional
    public PageResponse<CreditCardResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(creditCardRepository.findByUserIdAndActiveTrue(userId, pageable).map(card -> {
            ensureCurrentInvoice(card);
            return toResponse(card);
        }));
    }

    @Transactional
    public List<CreditCardResponse> listAll(UUID userId) {
        return creditCardRepository.findByUserIdAndActiveTrue(userId).stream().map(card -> {
            ensureCurrentInvoice(card);
            return toResponse(card);
        }).toList();
    }

    @Transactional
    public CreditCardResponse get(UUID userId, UUID id) {
        CreditCard card = getOwned(userId, id);
        ensureCurrentInvoice(card);
        return toResponse(card);
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
        ensureCurrentInvoice(card);
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
        BigDecimal used = MoneyUtils.of(
                invoiceRepository.sumAmountByCardAndStatuses(card.getId(), USED_LIMIT_STATUSES));
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

    private void ensureCurrentInvoice(CreditCard card) {
        if (invoiceRepository.findByCreditCardIdAndStatus(card.getId(), CreditCardInvoiceStatus.CURRENT).isPresent()) {
            return;
        }
        YearMonth period = CreditCardInvoiceService.currentInvoiceMonth(card, LocalDate.now());
        CreditCardInvoice existing = invoiceRepository
                .findByCreditCardIdAndReferenceYearAndReferenceMonth(
                        card.getId(), period.getYear(), period.getMonthValue())
                .orElse(null);
        if (existing != null) {
            existing.setStatus(CreditCardInvoiceStatus.CURRENT);
            invoiceRepository.save(existing);
            return;
        }
        invoiceRepository.save(CreditCardInvoice.builder()
                .creditCard(card)
                .referenceYear(period.getYear())
                .referenceMonth(period.getMonthValue())
                .amount(MoneyUtils.zero())
                .status(CreditCardInvoiceStatus.CURRENT)
                .build());
    }
}
