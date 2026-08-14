package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.response.ForecastResponse;
import br.net.convertix.dinix.entity.Income;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.RecurringExpense;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.enums.InvestmentTransactionType;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.repository.IncomeRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import br.net.convertix.dinix.repository.InvestmentTransactionRepository;
import br.net.convertix.dinix.util.DateUtils;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ForecastService {

    private final RecurringExpenseService recurringExpenseService;
    private final SubscriptionService subscriptionService;
    private final InstallmentRepository installmentRepository;
    private final IncomeRepository incomeRepository;
    private final InvestmentTransactionRepository investmentTransactionRepository;
    private final br.net.convertix.dinix.repository.FinancialTransactionRepository transactionRepository;

    public ForecastService(
            RecurringExpenseService recurringExpenseService,
            SubscriptionService subscriptionService,
            InstallmentRepository installmentRepository,
            IncomeRepository incomeRepository,
            InvestmentTransactionRepository investmentTransactionRepository,
            br.net.convertix.dinix.repository.FinancialTransactionRepository transactionRepository) {
        this.recurringExpenseService = recurringExpenseService;
        this.subscriptionService = subscriptionService;
        this.installmentRepository = installmentRepository;
        this.incomeRepository = incomeRepository;
        this.investmentTransactionRepository = investmentTransactionRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public ForecastResponse forecast(UUID userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal expectedIncome = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(
                userId, TransactionType.INCOME, start, end));
        for (Income income : incomeRepository.findByUserIdAndActiveTrueAndRecurringTrue(userId)) {
            if (!DateUtils.isInPeriod(income.getReceivedDate(), start, end)) {
                expectedIncome = expectedIncome.add(income.getAmount());
            }
        }

        BigDecimal committed = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(
                userId, TransactionType.EXPENSE, start, end));
        for (Installment installment : installmentRepository.findByPurchaseUserIdAndDueDateBetweenAndStatusIn(
                userId, start, end, List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE))) {
            if (installment.getPurchase().getPurchaseDate().isBefore(start)
                    || installment.getPurchase().getPurchaseDate().isAfter(end)) {
                // parcela já entra no ledger do mês de vencimento; evita somar de novo
            }
        }
        for (RecurringExpense expense : recurringExpenseService.activeOf(userId)) {
            if (recurringExpenseService.occursIn(expense, yearMonth)) {
                committed = committed.add(expense.getAmount());
            }
        }
        for (Subscription subscription : subscriptionService.activeOf(userId)) {
            if (subscriptionService.occursIn(subscription, yearMonth)) {
                committed = committed.add(subscription.getAmount());
            }
        }

        BigDecimal expectedInvestments = investmentTransactionRepository
                .findByInvestmentUserIdAndTransactionDateBetween(userId, start, end)
                .stream()
                .filter(tx -> tx.getType() == InvestmentTransactionType.BUY || tx.getType() == InvestmentTransactionType.DEPOSIT)
                .map(tx -> tx.getAmount())
                .reduce(MoneyUtils.zero(), BigDecimal::add);

        return new ForecastResponse(
                month, year, expectedIncome, committed, expectedInvestments,
                expectedIncome.subtract(committed).subtract(expectedInvestments));
    }
}
