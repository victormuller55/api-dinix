package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.response.CategoryBreakdownResponse;
import br.net.convertix.dinix.dto.response.CreditCardResponse;
import br.net.convertix.dinix.dto.response.DashboardResponse;
import br.net.convertix.dinix.dto.response.MonthlySummaryResponse;
import br.net.convertix.dinix.dto.response.NamedAmountResponse;
import br.net.convertix.dinix.dto.response.PeriodMetricResponse;
import br.net.convertix.dinix.dto.response.ProductStatResponse;
import br.net.convertix.dinix.dto.response.UpcomingPaymentResponse;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.RecurringExpense;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import br.net.convertix.dinix.repository.PurchaseItemRepository;
import br.net.convertix.dinix.repository.TransferRepository;
import br.net.convertix.dinix.util.DateUtils;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final FinancialTransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final InstallmentRepository installmentRepository;
    private final CreditCardService creditCardService;
    private final SubscriptionService subscriptionService;
    private final RecurringExpenseService recurringExpenseService;

    public DashboardService(
            FinancialTransactionRepository transactionRepository,
            TransferRepository transferRepository,
            PurchaseItemRepository purchaseItemRepository,
            InstallmentRepository installmentRepository,
            CreditCardService creditCardService,
            SubscriptionService subscriptionService,
            RecurringExpenseService recurringExpenseService) {
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.installmentRepository = installmentRepository;
        this.creditCardService = creditCardService;
        this.subscriptionService = subscriptionService;
        this.recurringExpenseService = recurringExpenseService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(UUID userId, int month, int year) {
        LocalDate start = DateUtils.startOfMonth(month, year);
        LocalDate end = DateUtils.endOfMonth(month, year);
        YearMonth previous = DateUtils.previous(month, year);

        PeriodMetricResponse income = metric(userId, TransactionType.INCOME, start, end, previous);
        PeriodMetricResponse expenses = metric(userId, TransactionType.EXPENSE, start, end, previous);
        BigDecimal investments = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(
                userId, TransactionType.INVESTMENT, start, end));
        BigDecimal available = income.total().subtract(expenses.total()).subtract(investments);

        List<CreditCardResponse> cards = creditCardService.listAll(userId);
        List<UpcomingPaymentResponse> upcoming = upcoming(userId, YearMonth.of(year, month));
        return new DashboardResponse(
                month, year, income, expenses, investments, available,
                expensesByCategory(userId, start, end), upcoming, cards,
                subscriptionService.listActive(userId)
        );
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(UUID userId, int month, int year) {
        LocalDate start = DateUtils.startOfMonth(month, year);
        LocalDate end = DateUtils.endOfMonth(month, year);
        YearMonth previous = DateUtils.previous(month, year);
        PeriodMetricResponse income = metric(userId, TransactionType.INCOME, start, end, previous);
        PeriodMetricResponse expenses = metric(userId, TransactionType.EXPENSE, start, end, previous);
        BigDecimal investments = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(
                userId, TransactionType.INVESTMENT, start, end));
        BigDecimal transfers = transferRepository
                .findByUserIdAndActiveTrueAndTransferDateBetween(userId, start, end, Pageable.unpaged())
                .getContent()
                .stream()
                .map(t -> t.getAmount())
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        BigDecimal available = income.total().subtract(expenses.total()).subtract(investments);
        BigDecimal base = income.total().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : income.total();
        return new MonthlySummaryResponse(
                month, year, income.total(), expenses.total(), investments, transfers, available,
                MoneyUtils.percentage(expenses.total(), base),
                MoneyUtils.percentage(investments, base),
                expenses,
                expensesByCategory(userId, start, end),
                topLocations(userId, start, end),
                topProducts(userId, start, end)
        );
    }

    public PeriodMetricResponse metric(UUID userId, TransactionType type, LocalDate start, LocalDate end, YearMonth previous) {
        BigDecimal total = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(userId, type, start, end));
        long count = transactionRepository.countByTypeInPeriod(userId, type, start, end);
        BigDecimal previousTotal = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(
                userId, type, previous.atDay(1), previous.atEndOfMonth()));
        return new PeriodMetricResponse(total, count, previousTotal, MoneyUtils.percentage(total.subtract(previousTotal), previousTotal));
    }

    public List<CategoryBreakdownResponse> expensesByCategory(UUID userId, LocalDate start, LocalDate end) {
        List<Object[]> rows = transactionRepository.sumExpensesByCategory(userId, start, end);
        BigDecimal total = rows.stream()
                .map(row -> MoneyUtils.of((BigDecimal) row[1]))
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        return rows.stream()
                .map(row -> new CategoryBreakdownResponse(
                        String.valueOf(row[0]),
                        MoneyUtils.of((BigDecimal) row[1]),
                        MoneyUtils.percentage((BigDecimal) row[1], total)))
                .toList();
    }

    public List<NamedAmountResponse> topLocations(UUID userId, LocalDate start, LocalDate end) {
        return transactionRepository.topLocations(userId, start, end).stream()
                .map(row -> new NamedAmountResponse(String.valueOf(row[0]), MoneyUtils.of((BigDecimal) row[1])))
                .toList();
    }

    public List<ProductStatResponse> topProducts(UUID userId, LocalDate start, LocalDate end) {
        return purchaseItemRepository.summarizeProducts(userId, start, end).stream()
                .map(row -> new ProductStatResponse(
                        (UUID) row[0],
                        String.valueOf(row[1]),
                        (BigDecimal) row[2],
                        MoneyUtils.of((BigDecimal) row[3]),
                        MoneyUtils.of((BigDecimal) row[4]),
                        (LocalDate) row[5]
                ))
                .toList();
    }

    private List<UpcomingPaymentResponse> upcoming(UUID userId, YearMonth month) {
        List<UpcomingPaymentResponse> items = new ArrayList<>();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        for (Installment installment : installmentRepository.findByPurchaseUserIdAndDueDateBetweenAndStatusIn(
                userId, start, end, List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE))) {
            items.add(new UpcomingPaymentResponse(
                    "parcela",
                    installment.getPurchase().getDescription() + " " + installment.getInstallmentNumber()
                            + "/" + installment.getTotalInstallments(),
                    installment.getAmount(),
                    installment.getDueDate()));
        }
        for (Subscription subscription : subscriptionService.activeOf(userId)) {
            if (subscriptionService.occursIn(subscription, month)) {
                items.add(new UpcomingPaymentResponse(
                        "assinatura",
                        subscription.getName(),
                        subscription.getAmount(),
                        MoneyUtils.atDayOfMonth(month, subscription.getBillingDay())));
            }
        }
        for (RecurringExpense expense : recurringExpenseService.activeOf(userId)) {
            if (recurringExpenseService.occursIn(expense, month)) {
                items.add(new UpcomingPaymentResponse(
                        "despesa_recorrente",
                        expense.getName(),
                        expense.getAmount(),
                        MoneyUtils.atDayOfMonth(month, expense.getDueDay())));
            }
        }
        return items;
    }
}
