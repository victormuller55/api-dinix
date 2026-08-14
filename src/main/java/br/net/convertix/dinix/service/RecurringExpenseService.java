package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateRecurringExpenseRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.RecurringExpenseResponse;
import br.net.convertix.dinix.entity.RecurringExpense;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
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

    public RecurringExpenseService(
            RecurringExpenseRepository repository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService) {
        this.repository = repository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @Transactional
    public RecurringExpenseResponse create(UUID userId, CreateRecurringExpenseRequest request) {
        User user = authService.getActive(userId);
        RecurringExpense expense = RecurringExpense.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .amount(MoneyUtils.of(request.amount()))
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .account(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null)
                .dueDay(request.dueDay())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .recurrence(request.recurrence() != null ? request.recurrence() : RecurrenceType.MONTHLY)
                .active(true)
                .build();
        return toResponse(repository.save(expense));
    }

    @Transactional(readOnly = true)
    public PageResponse<RecurringExpenseResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RecurringExpenseResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public RecurringExpenseResponse update(UUID userId, UUID id, CreateRecurringExpenseRequest request) {
        RecurringExpense expense = getOwned(userId, id);
        expense.setName(request.name());
        expense.setDescription(request.description());
        expense.setAmount(MoneyUtils.of(request.amount()));
        expense.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        expense.setAccount(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null);
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

    public List<RecurringExpense> activeOf(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    public boolean occursIn(RecurringExpense expense, YearMonth month) {
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

    private RecurringExpense getOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta recorrente não encontrada"));
    }

    private RecurringExpenseResponse toResponse(RecurringExpense expense) {
        return new RecurringExpenseResponse(
                expense.getId(), expense.getName(), expense.getDescription(), expense.getAmount(),
                expense.getCategory() != null ? expense.getCategory().getId() : null,
                expense.getAccount() != null ? expense.getAccount().getId() : null,
                expense.getDueDay(), expense.getStartDate(), expense.getEndDate(),
                expense.getRecurrence(), expense.isActive(), expense.getCreatedAt(), expense.getUpdatedAt());
    }
}
