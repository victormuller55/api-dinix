package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateIncomeRequest;
import br.net.convertix.dinix.dto.response.IncomeResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.Income;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.IncomeRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AuthService authService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final LedgerService ledgerService;

    public IncomeService(
            IncomeRepository incomeRepository,
            FinancialTransactionRepository transactionRepository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService,
            LedgerService ledgerService) {
        this.incomeRepository = incomeRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public IncomeResponse create(UUID userId, CreateIncomeRequest request) {
        User user = authService.getActive(userId);
        Income income = Income.builder()
                .user(user)
                .description(request.description())
                .amount(MoneyUtils.of(request.amount()))
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .account(accountService.getOwned(userId, request.accountId()))
                .receivedDate(request.receivedDate())
                .recurring(request.recurring())
                .notes(request.notes())
                .active(true)
                .build();
        Income saved = incomeRepository.save(income);
        ledgerService.postIncome(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<IncomeResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(incomeRepository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public IncomeResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public IncomeResponse update(UUID userId, UUID id, CreateIncomeRequest request) {
        Income income = getOwned(userId, id);
        transactionRepository.findByIncomeIdAndActiveTrue(income.getId()).forEach(ledgerService::reverse);
        income.setDescription(request.description());
        income.setAmount(MoneyUtils.of(request.amount()));
        income.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        income.setAccount(accountService.getOwned(userId, request.accountId()));
        income.setReceivedDate(request.receivedDate());
        income.setRecurring(request.recurring());
        income.setNotes(request.notes());
        Income saved = incomeRepository.save(income);
        ledgerService.postIncome(saved);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Income income = getOwned(userId, id);
        income.setActive(false);
        transactionRepository.findByIncomeIdAndActiveTrue(income.getId()).forEach(ledgerService::reverse);
        incomeRepository.save(income);
    }

    public Income getOwned(UUID userId, UUID id) {
        return incomeRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada"));
    }

    private IncomeResponse toResponse(Income income) {
        return new IncomeResponse(
                income.getId(), income.getDescription(), income.getAmount(),
                income.getCategory() != null ? income.getCategory().getId() : null,
                income.getAccount().getId(), income.getReceivedDate(), income.isRecurring(),
                income.getNotes(), income.getCreatedAt(), income.getUpdatedAt());
    }
}
