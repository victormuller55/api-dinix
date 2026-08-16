package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateRecurringIncomeRequest;
import br.net.convertix.dinix.dto.request.ReceiveRecurringIncomeRequest;
import br.net.convertix.dinix.dto.response.IncomeResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.RecurringIncomeResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.Income;
import br.net.convertix.dinix.entity.RecurringIncome;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.IncomeRepository;
import br.net.convertix.dinix.repository.RecurringIncomeRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringIncomeService {

    private final RecurringIncomeRepository repository;
    private final IncomeRepository incomeRepository;
    private final AuthService authService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final LedgerService ledgerService;

    public RecurringIncomeService(
            RecurringIncomeRepository repository,
            IncomeRepository incomeRepository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService,
            LedgerService ledgerService) {
        this.repository = repository;
        this.incomeRepository = incomeRepository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public RecurringIncomeResponse create(UUID userId, CreateRecurringIncomeRequest request) {
        User user = authService.getActive(userId);
        FinancialAccount account = accountService.getOwned(userId, request.accountId());
        RecurringIncome income = RecurringIncome.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .amount(MoneyUtils.of(request.amount()))
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .account(account)
                .receiveDay(request.receiveDay())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .recurrence(request.recurrence() != null ? request.recurrence() : RecurrenceType.MONTHLY)
                .active(true)
                .build();
        RecurringIncome saved = repository.save(income);
        if (Boolean.TRUE.equals(request.receiveToday())) {
            receive(saved, LocalDate.now(), account);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<RecurringIncomeResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RecurringIncomeResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional(readOnly = true)
    public List<RecurringIncomeResponse> pendingOn(UUID userId, LocalDate date) {
        return activeOf(userId).stream()
                .filter(income -> isPendingOn(income, date))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecurringIncomeResponse update(UUID userId, UUID id, CreateRecurringIncomeRequest request) {
        RecurringIncome income = getOwned(userId, id);
        income.setName(request.name());
        income.setDescription(request.description());
        income.setAmount(MoneyUtils.of(request.amount()));
        income.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        income.setAccount(accountService.getOwned(userId, request.accountId()));
        income.setReceiveDay(request.receiveDay());
        income.setStartDate(request.startDate());
        income.setEndDate(request.endDate());
        income.setRecurrence(request.recurrence() != null ? request.recurrence() : income.getRecurrence());
        return toResponse(repository.save(income));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        RecurringIncome income = getOwned(userId, id);
        income.setActive(false);
        repository.save(income);
    }

    @Transactional
    public IncomeResponse receive(
            UUID userId, UUID id, ReceiveRecurringIncomeRequest request, LocalDate referenceDate) {
        RecurringIncome template = getOwned(userId, id);
        YearMonth month = YearMonth.from(referenceDate);
        if (!occursIn(template, month)) {
            throw new BusinessException("Este recebimento mensal não está ativo para o mês selecionado");
        }
        if (isReceivedFor(template, month)) {
            throw new BusinessException("Este recebimento mensal já foi marcado neste mês");
        }
        if (referenceDate.getDayOfMonth() < 1) {
            throw new BusinessException("Só é possível marcar como recebido a partir do dia 1 do mês");
        }
        FinancialAccount account = accountService.getOwned(userId, request.accountId());
        template.setAccount(account);
        Income created = receive(template, referenceDate, account);
        return new IncomeResponse(
                created.getId(),
                created.getDescription(),
                created.getAmount(),
                created.getCategory() != null ? created.getCategory().getId() : null,
                created.getAccount().getId(),
                created.getReceivedDate(),
                created.isRecurring(),
                created.getNotes(),
                created.getCreatedAt(),
                created.getUpdatedAt());
    }

    public List<RecurringIncome> activeOf(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    public boolean occursIn(RecurringIncome income, YearMonth month) {
        LocalDate start = income.getStartDate();
        LocalDate end = income.getEndDate();
        LocalDate occurrence = MoneyUtils.atDayOfMonth(month, income.getReceiveDay());
        if (occurrence.isBefore(start)) {
            return false;
        }
        if (end != null && occurrence.isAfter(end)) {
            return false;
        }
        return switch (income.getRecurrence()) {
            case MONTHLY, CUSTOM, WEEKLY -> true;
            case YEARLY -> occurrence.getMonthValue() == start.getMonthValue();
        };
    }

    public boolean isPendingOn(RecurringIncome income, LocalDate date) {
        YearMonth month = YearMonth.from(date);
        if (!occursIn(income, month)) {
            return false;
        }
        // Disponível a partir do dia 1 do mês.
        if (date.getDayOfMonth() < 1) {
            return false;
        }
        return !isReceivedFor(income, month);
    }

    private Income receive(RecurringIncome template, LocalDate receivedDate, FinancialAccount account) {
        YearMonth month = YearMonth.from(receivedDate);
        Income income = Income.builder()
                .user(template.getUser())
                .description(template.getName())
                .amount(template.getAmount())
                .category(template.getCategory())
                .account(account)
                .receivedDate(receivedDate)
                .recurring(true)
                .notes(template.getDescription())
                .active(true)
                .build();
        Income saved = incomeRepository.save(income);
        ledgerService.postIncome(saved);
        template.setLastReceivedYear(month.getYear());
        template.setLastReceivedMonth(month.getMonthValue());
        repository.save(template);
        return saved;
    }

    private boolean isReceivedFor(RecurringIncome income, YearMonth month) {
        return income.getLastReceivedYear() != null
                && income.getLastReceivedMonth() != null
                && income.getLastReceivedYear() == month.getYear()
                && income.getLastReceivedMonth() == month.getMonthValue();
    }

    private RecurringIncome getOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recebimento mensal não encontrado"));
    }

    private RecurringIncomeResponse toResponse(RecurringIncome income) {
        return new RecurringIncomeResponse(
                income.getId(),
                income.getName(),
                income.getDescription(),
                income.getAmount(),
                income.getCategory() != null ? income.getCategory().getId() : null,
                income.getAccount().getId(),
                income.getReceiveDay(),
                income.getStartDate(),
                income.getEndDate(),
                income.getRecurrence(),
                income.getLastReceivedYear(),
                income.getLastReceivedMonth(),
                income.isActive(),
                income.getCreatedAt(),
                income.getUpdatedAt());
    }
}
