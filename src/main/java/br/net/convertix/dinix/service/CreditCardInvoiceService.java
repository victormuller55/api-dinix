package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.request.PayCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.request.UpdateCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.response.CreditCardInvoiceResponse;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.CreditCardInvoice;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.enums.CreditCardInvoiceStatus;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.CreditCardInvoiceRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class CreditCardInvoiceService {

    private static final EnumSet<CreditCardInvoiceStatus> USED_LIMIT_STATUSES = EnumSet.of(
            CreditCardInvoiceStatus.CURRENT,
            CreditCardInvoiceStatus.UPCOMING,
            CreditCardInvoiceStatus.CLOSED
    );

    private final CreditCardInvoiceRepository invoiceRepository;
    private final CreditCardService creditCardService;
    private final AccountService accountService;
    private final LedgerService ledgerService;

    public CreditCardInvoiceService(
            CreditCardInvoiceRepository invoiceRepository,
            CreditCardService creditCardService,
            AccountService accountService,
            LedgerService ledgerService) {
        this.invoiceRepository = invoiceRepository;
        this.creditCardService = creditCardService;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @Transactional(readOnly = true)
    public List<CreditCardInvoiceResponse> list(UUID userId, UUID creditCardId) {
        creditCardService.getOwned(userId, creditCardId);
        return invoiceRepository.findByCreditCardIdOrderByReferenceYearAscReferenceMonthAsc(creditCardId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CreditCardInvoiceResponse create(UUID userId, UUID creditCardId, CreateCreditCardInvoiceRequest request) {
        CreditCard card = creditCardService.getOwned(userId, creditCardId);
        YearMonth period = YearMonth.of(request.year(), request.month());
        invoiceRepository.findByCreditCardIdAndReferenceYearAndReferenceMonth(
                        creditCardId, period.getYear(), period.getMonthValue())
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe fatura para " + period);
                });

        boolean hasCurrent = invoiceRepository.findByCreditCardIdAndStatus(creditCardId, CreditCardInvoiceStatus.CURRENT)
                .isPresent();
        YearMonth currentPeriod = currentInvoiceMonth(card, LocalDate.now());
        CreditCardInvoiceStatus status;
        if (!hasCurrent && !period.isBefore(currentPeriod)) {
            status = period.equals(currentPeriod) ? CreditCardInvoiceStatus.CURRENT : CreditCardInvoiceStatus.UPCOMING;
        } else if (!hasCurrent) {
            status = CreditCardInvoiceStatus.CURRENT;
        } else {
            status = period.isBefore(currentPeriod) ? CreditCardInvoiceStatus.CLOSED : CreditCardInvoiceStatus.UPCOMING;
        }

        CreditCardInvoice invoice = CreditCardInvoice.builder()
                .creditCard(card)
                .referenceYear(period.getYear())
                .referenceMonth(period.getMonthValue())
                .amount(MoneyUtils.of(request.amount()))
                .status(status)
                .build();
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public CreditCardInvoiceResponse update(UUID userId, UUID invoiceId, UpdateCreditCardInvoiceRequest request) {
        CreditCardInvoice invoice = getOwned(userId, invoiceId);
        if (invoice.getStatus() == CreditCardInvoiceStatus.PAID) {
            throw new BusinessException("Não é possível alterar uma fatura paga");
        }
        invoice.setAmount(MoneyUtils.of(request.amount()));
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void delete(UUID userId, UUID invoiceId) {
        CreditCardInvoice invoice = getOwned(userId, invoiceId);
        if (invoice.getStatus() == CreditCardInvoiceStatus.CURRENT) {
            throw new BusinessException("Não é possível excluir a fatura atual. Feche-a ou edite o valor.");
        }
        invoiceRepository.delete(invoice);
    }

    @Transactional
    public List<CreditCardInvoiceResponse> closeCurrent(UUID userId, UUID creditCardId) {
        CreditCard card = creditCardService.getOwned(userId, creditCardId);
        CreditCardInvoice current = invoiceRepository
                .findByCreditCardIdAndStatus(creditCardId, CreditCardInvoiceStatus.CURRENT)
                .orElseThrow(() -> new BusinessException("Não há fatura atual para fechar"));

        current.setStatus(CreditCardInvoiceStatus.CLOSED);
        invoiceRepository.save(current);

        YearMonth nextPeriod = YearMonth.of(current.getReferenceYear(), current.getReferenceMonth()).plusMonths(1);
        CreditCardInvoice next = invoiceRepository
                .findByCreditCardIdAndReferenceYearAndReferenceMonth(
                        creditCardId, nextPeriod.getYear(), nextPeriod.getMonthValue())
                .orElseGet(() -> CreditCardInvoice.builder()
                        .creditCard(card)
                        .referenceYear(nextPeriod.getYear())
                        .referenceMonth(nextPeriod.getMonthValue())
                        .amount(MoneyUtils.zero())
                        .status(CreditCardInvoiceStatus.UPCOMING)
                        .build());

        next.setStatus(CreditCardInvoiceStatus.CURRENT);
        invoiceRepository.save(next);

        return list(userId, creditCardId);
    }

    @Transactional
    public CreditCardInvoiceResponse markPaid(
            UUID userId,
            UUID invoiceId,
            PayCreditCardInvoiceRequest request) {
        CreditCardInvoice invoice = getOwned(userId, invoiceId);
        if (invoice.getStatus() == CreditCardInvoiceStatus.CURRENT) {
            throw new BusinessException("Feche a fatura atual antes de marcá-la como paga");
        }
        if (invoice.getStatus() == CreditCardInvoiceStatus.PAID) {
            throw new BusinessException("Fatura já está paga");
        }
        if (request.accountId() == null) {
            throw new BusinessException("Selecione a conta de onde saiu o pagamento");
        }

        FinancialAccount account = accountService.getOwned(userId, request.accountId());
        BigDecimal amount = MoneyUtils.of(invoice.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            CreditCard card = invoice.getCreditCard();
            String cardName = card.getName() != null ? card.getName() : "cartão";
            ledgerService.postCardPayment(
                    account.getUser(),
                    account,
                    amount,
                    LocalDate.now(),
                    "Pagamento fatura " + invoice.getReferenceMonth() + "/" + invoice.getReferenceYear()
                            + " - " + cardName);
        }

        invoice.setStatus(CreditCardInvoiceStatus.PAID);
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void ensureCurrentInvoice(CreditCard card) {
        UUID cardId = card.getId();
        if (invoiceRepository.findByCreditCardIdAndStatus(cardId, CreditCardInvoiceStatus.CURRENT).isPresent()) {
            return;
        }
        YearMonth period = currentInvoiceMonth(card, LocalDate.now());
        CreditCardInvoice existing = invoiceRepository
                .findByCreditCardIdAndReferenceYearAndReferenceMonth(cardId, period.getYear(), period.getMonthValue())
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

    @Transactional
    public void addAmount(CreditCard card, YearMonth period, BigDecimal delta) {
        if (card == null || delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        CreditCardInvoice invoice = findOrCreateForPeriod(card, period);
        invoice.setAmount(MoneyUtils.of(invoice.getAmount().add(delta)));
        if (invoice.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            invoice.setAmount(MoneyUtils.zero());
        }
        invoiceRepository.save(invoice);
    }

    public BigDecimal sumUsedLimit(UUID creditCardId) {
        return MoneyUtils.of(invoiceRepository.sumAmountByCardAndStatuses(creditCardId, USED_LIMIT_STATUSES));
    }

    public static YearMonth currentInvoiceMonth(CreditCard card, LocalDate today) {
        if (today.getDayOfMonth() <= card.getClosingDay()) {
            return YearMonth.from(today);
        }
        return YearMonth.from(today).plusMonths(1);
    }

    private CreditCardInvoice findOrCreateForPeriod(CreditCard card, YearMonth period) {
        return invoiceRepository
                .findByCreditCardIdAndReferenceYearAndReferenceMonth(
                        card.getId(), period.getYear(), period.getMonthValue())
                .orElseGet(() -> {
                    CreditCardInvoiceStatus status = resolveStatusForNew(card, period);
                    return invoiceRepository.save(CreditCardInvoice.builder()
                            .creditCard(card)
                            .referenceYear(period.getYear())
                            .referenceMonth(period.getMonthValue())
                            .amount(MoneyUtils.zero())
                            .status(status)
                            .build());
                });
    }

    private CreditCardInvoiceStatus resolveStatusForNew(CreditCard card, YearMonth period) {
        var current = invoiceRepository.findByCreditCardIdAndStatus(card.getId(), CreditCardInvoiceStatus.CURRENT);
        if (current.isEmpty()) {
            YearMonth currentPeriod = currentInvoiceMonth(card, LocalDate.now());
            return period.equals(currentPeriod) ? CreditCardInvoiceStatus.CURRENT : CreditCardInvoiceStatus.UPCOMING;
        }
        YearMonth currentPeriod = YearMonth.of(
                current.get().getReferenceYear(), current.get().getReferenceMonth());
        if (period.equals(currentPeriod)) {
            return CreditCardInvoiceStatus.CURRENT;
        }
        if (period.isBefore(currentPeriod)) {
            return CreditCardInvoiceStatus.CLOSED;
        }
        return CreditCardInvoiceStatus.UPCOMING;
    }

    private CreditCardInvoice getOwned(UUID userId, UUID invoiceId) {
        return invoiceRepository.findByIdAndCreditCardUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));
    }

    private CreditCardInvoiceResponse toResponse(CreditCardInvoice invoice) {
        return new CreditCardInvoiceResponse(
                invoice.getId(),
                invoice.getCreditCard().getId(),
                invoice.getReferenceYear(),
                invoice.getReferenceMonth(),
                invoice.getAmount(),
                invoice.getStatus(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
