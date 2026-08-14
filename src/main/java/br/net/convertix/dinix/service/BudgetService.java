package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateBudgetRequest;
import br.net.convertix.dinix.dto.request.UpdateBudgetRequest;
import br.net.convertix.dinix.dto.response.BudgetResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.Budget;
import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.ConflictException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.BudgetRepository;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.util.DateUtils;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AuthService authService;
    private final CategoryService categoryService;

    public BudgetService(
            BudgetRepository budgetRepository,
            FinancialTransactionRepository transactionRepository,
            AuthService authService,
            CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.categoryService = categoryService;
    }

    @Transactional
    public BudgetResponse create(UUID userId, CreateBudgetRequest request) {
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.categoryId(), request.month(), request.year())) {
            throw new ConflictException("Já existe orçamento para essa categoria no período");
        }
        User user = authService.getActive(userId);
        Category category = categoryService.getOwned(userId, request.categoryId());
        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amountLimit(MoneyUtils.of(request.amountLimit()))
                .month(request.month())
                .year(request.year())
                .build();
        return toResponse(budgetRepository.save(budget), userId);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(UUID userId, int month, int year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(budget -> toResponse(budget, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<BudgetResponse> list(UUID userId, int month, int year, Pageable pageable) {
        return PageResponse.from(
                budgetRepository.findByUserIdAndMonthAndYear(userId, month, year, pageable)
                        .map(budget -> toResponse(budget, userId)));
    }

    @Transactional
    public BudgetResponse update(UUID userId, UUID id, UpdateBudgetRequest request) {
        Budget budget = getOwned(userId, id);
        budget.setAmountLimit(MoneyUtils.of(request.amountLimit()));
        return toResponse(budgetRepository.save(budget), userId);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        budgetRepository.delete(getOwned(userId, id));
    }

    private Budget getOwned(UUID userId, UUID id) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
    }

    private BudgetResponse toResponse(Budget budget, UUID userId) {
        BigDecimal categorySpent = spentByCategory(userId, budget);
        BigDecimal remaining = budget.getAmountLimit().subtract(categorySpent);
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmountLimit(),
                categorySpent,
                remaining,
                MoneyUtils.percentage(categorySpent, budget.getAmountLimit()),
                budget.getMonth(),
                budget.getYear()
        );
    }

    private BigDecimal spentByCategory(UUID userId, Budget budget) {
        List<Object[]> rows = transactionRepository.sumExpensesByCategory(
                userId,
                DateUtils.startOfMonth(budget.getMonth(), budget.getYear()),
                DateUtils.endOfMonth(budget.getMonth(), budget.getYear()));
        return rows.stream()
                .filter(row -> budget.getCategory().getName().equals(row[0]))
                .map(row -> MoneyUtils.of((BigDecimal) row[1]))
                .findFirst()
                .orElse(MoneyUtils.zero());
    }
}
