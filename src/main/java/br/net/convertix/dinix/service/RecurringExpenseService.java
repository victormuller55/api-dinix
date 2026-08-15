package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateRecurringExpenseRequest;
import br.net.convertix.dinix.dto.request.PayRecurringExpenseRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.PurchaseResponse;
import br.net.convertix.dinix.dto.response.RecurringExpenseResponse;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.RecurringExpense;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.mapper.PurchaseMapper;
import br.net.convertix.dinix.repository.RecurringExpenseRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringExpenseService {

    private final RecurringExpenseRepository repository;
    private final AuthService authService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final CreditCardService creditCardService;
    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;

    public RecurringExpenseService(
            RecurringExpenseRepository repository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService,
            CreditCardService creditCardService,
            PurchaseService purchaseService,
            PurchaseMapper purchaseMapper) {
        this.repository = repository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.creditCardService = creditCardService;
        this.purchaseService = purchaseService;
        this.purchaseMapper = purchaseMapper;
    }

    @Transactional
    public RecurringExpenseResponse create(UUID userId, CreateRecurringExpenseRequest request) {
        validatePayment(request.paymentMethod(), request.accountId(), request.creditCardId());
        User user = authService.getActive(userId);
        RecurringExpense expense = RecurringExpense.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .amount(MoneyUtils.of(request.amount()))
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .paymentMethod(request.paymentMethod())
                .account(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null)
                .creditCard(creditCardService.getOwnedOrNull(userId, request.creditCardId()))
                .dueDay(request.dueDay())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .recurrence(request.recurrence() != null ? request.recurrence() : RecurrenceType.MONTHLY)
                .active(true)
                .build();
        RecurringExpense saved = repository.save(expense);
        if (Boolean.TRUE.equals(request.chargeToday())) {
            YearMonth month = YearMonth.from(LocalDate.now());
            pay(saved, LocalDate.now(), request.paymentMethod(), saved.getAccount(), saved.getCreditCard(), month);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<RecurringExpenseResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RecurringExpenseResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> pendingOn(UUID userId, LocalDate date) {
        return activeOf(userId).stream()
                .filter(expense -> isPendingOn(expense, date))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecurringExpenseResponse update(UUID userId, UUID id, CreateRecurringExpenseRequest request) {
        validatePayment(request.paymentMethod(), request.accountId(), request.creditCardId());
        RecurringExpense expense = getOwned(userId, id);
        expense.setName(request.name());
        expense.setDescription(request.description());
        expense.setAmount(MoneyUtils.of(request.amount()));
        expense.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        expense.setPaymentMethod(request.paymentMethod());
        expense.setAccount(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null);
        expense.setCreditCard(creditCardService.getOwnedOrNull(userId, request.creditCardId()));
        expense.setDueDay(request.dueDay());
        expense.setStartDate(request.startDate());
        expense.setEndDate(request.endDate());
        expense.setRecurrence(request.recurrence() != null ? request.recurrence() : expense.getRecurrence());
        return toResponse(repository.save(expense));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        RecurringExpense expense = getOwned(userId, id);
        expense.setActive(false);
        repository.save(expense);
    }

    @Transactional
    public PurchaseResponse pay(
            UUID userId, UUID id, PayRecurringExpenseRequest request, LocalDate referenceDate) {
        RecurringExpense expense = getOwned(userId, id);
        YearMonth month = YearMonth.from(referenceDate);
        if (!isDueInMonth(expense, month)) {
            throw new BusinessException("Este gasto mensal não está ativo para o mês selecionado");
        }
        if (isPaidFor(expense, month)) {
            throw new BusinessException("Este gasto mensal já foi pago neste mês");
        }
        LocalDate dueDate = MoneyUtils.atDayOfMonth(month, expense.getDueDay());
        if (!referenceDate.equals(dueDate)) {
            throw new BusinessException("Este gasto mensal não está pendente na data selecionada");
        }
        validatePayment(request.paymentMethod(), request.financialAccountId(), request.creditCardId());
        FinancialAccount account = request.financialAccountId() != null
                ? accountService.getOwned(userId, request.financialAccountId())
                : null;
        CreditCard card = creditCardService.getOwnedOrNull(userId, request.creditCardId());
        expense.setPaymentMethod(request.paymentMethod());
        expense.setAccount(request.paymentMethod() == PaymentMethod.CREDIT_CARD ? null : account);
        expense.setCreditCard(request.paymentMethod() == PaymentMethod.CREDIT_CARD ? card : null);
        LocalDate chargeDate = LocalDate.now().isBefore(dueDate) ? dueDate : LocalDate.now();
        Purchase purchase = pay(expense, chargeDate, request.paymentMethod(), account, card, month);
        return purchaseMapper.toResponse(purchase);
    }

    public List<RecurringExpense> activeOf(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    public boolean occursIn(RecurringExpense expense, YearMonth month) {
        return isDueInMonth(expense, month);
    }

    public boolean isPendingOn(RecurringExpense expense, LocalDate date) {
        YearMonth month = YearMonth.from(date);
        if (!isDueInMonth(expense, month)) {
            return false;
        }
        LocalDate dueDate = MoneyUtils.atDayOfMonth(month, expense.getDueDay());
        if (!date.equals(dueDate)) {
            return false;
        }
        return !isPaidFor(expense, month);
    }

    private Purchase pay(
            RecurringExpense expense,
            LocalDate chargeDate,
            PaymentMethod method,
            FinancialAccount account,
            CreditCard card,
            YearMonth paidMonth) {
        Purchase purchase = purchaseService.chargeRecurringExpense(expense, chargeDate, method, account, card);
        expense.setLastPaidYear(paidMonth.getYear());
        expense.setLastPaidMonth(paidMonth.getMonthValue());
        repository.save(expense);
        return purchase;
    }

    private boolean isDueInMonth(RecurringExpense expense, YearMonth month) {
        LocalDate start = expense.getStartDate();
        LocalDate end = expense.getEndDate();
        LocalDate occurrence = MoneyUtils.atDayOfMonth(month, expense.getDueDay());
        if (occurrence.isBefore(start)) {
            return false;
        }
        if (end != null && occurrence.isAfter(end)) {
            return false;
        }
        return switch (expense.getRecurrence()) {
            case MONTHLY, CUSTOM -> true;
            case YEARLY -> occurrence.getMonthValue() == start.getMonthValue();
            case WEEKLY -> true;
        };
    }

    private boolean isPaidFor(RecurringExpense expense, YearMonth month) {
        return expense.getLastPaidYear() != null
                && expense.getLastPaidMonth() != null
                && expense.getLastPaidYear() == month.getYear()
                && expense.getLastPaidMonth() == month.getMonthValue();
    }

    private void validatePayment(PaymentMethod method, UUID accountId, UUID creditCardId) {
        if (method == PaymentMethod.CREDIT_CARD && creditCardId == null) {
            throw new BusinessException("Cartão de crédito é obrigatório para essa forma de pagamento");
        }
        if (method != PaymentMethod.CREDIT_CARD && accountId == null) {
            throw new BusinessException("Conta financeira é obrigatória para essa forma de pagamento");
        }
    }

    private RecurringExpense getOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto mensal não encontrado"));
    }

    private RecurringExpenseResponse toResponse(RecurringExpense expense) {
        return new RecurringExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory() != null ? expense.getCategory().getId() : null,
                expense.getPaymentMethod(),
                expense.getAccount() != null ? expense.getAccount().getId() : null,
                expense.getCreditCard() != null ? expense.getCreditCard().getId() : null,
                expense.getDueDay(),
                expense.getStartDate(),
                expense.getEndDate(),
                expense.getRecurrence(),
                expense.getLastPaidYear(),
                expense.getLastPaidMonth(),
                expense.isActive(),
                expense.getCreatedAt(),
                expense.getUpdatedAt());
    }
}
