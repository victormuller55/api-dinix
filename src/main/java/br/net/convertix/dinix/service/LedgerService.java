package br.net.convertix.dinix.service;

import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.FinancialTransaction;
import br.net.convertix.dinix.entity.Income;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.InvestmentTransaction;
import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.Transfer;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.repository.FinancialAccountRepository;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class LedgerService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinancialAccountRepository accountRepository;

    public LedgerService(
            FinancialTransactionRepository transactionRepository,
            FinancialAccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public FinancialTransaction postExpense(
            User user,
            BigDecimal amount,
            LocalDate date,
            String description,
            FinancialAccount account,
            CreditCard creditCard,
            Category category,
            Purchase purchase,
            Installment installment,
            boolean affectsAccountBalance) {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .user(user)
                .type(TransactionType.EXPENSE)
                .amount(MoneyUtils.of(amount))
                .transactionDate(date)
                .description(description)
                .account(account)
                .creditCard(creditCard)
                .category(category)
                .purchase(purchase)
                .installment(installment)
                .countsInMonthlyResult(true)
                .affectsAccountBalance(affectsAccountBalance)
                .inflow(false)
                .active(true)
                .build();
        return persist(transaction);
    }

    @Transactional
    public FinancialTransaction postIncome(Income income) {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .user(income.getUser())
                .type(TransactionType.INCOME)
                .amount(MoneyUtils.of(income.getAmount()))
                .transactionDate(income.getReceivedDate())
                .description(income.getDescription())
                .account(income.getAccount())
                .category(income.getCategory())
                .income(income)
                .countsInMonthlyResult(true)
                .affectsAccountBalance(true)
                .inflow(true)
                .active(true)
                .build();
        return persist(transaction);
    }

    @Transactional
    public void postTransfer(Transfer transfer) {
        persist(FinancialTransaction.builder()
                .user(transfer.getUser())
                .type(TransactionType.TRANSFER)
                .amount(MoneyUtils.of(transfer.getAmount()))
                .transactionDate(transfer.getTransferDate())
                .description(transfer.getDescription())
                .account(transfer.getSourceAccount())
                .transfer(transfer)
                .countsInMonthlyResult(false)
                .affectsAccountBalance(true)
                .inflow(false)
                .active(true)
                .build());
        persist(FinancialTransaction.builder()
                .user(transfer.getUser())
                .type(TransactionType.TRANSFER)
                .amount(MoneyUtils.of(transfer.getAmount()))
                .transactionDate(transfer.getTransferDate())
                .description(transfer.getDescription())
                .account(transfer.getDestinationAccount())
                .transfer(transfer)
                .countsInMonthlyResult(false)
                .affectsAccountBalance(true)
                .inflow(true)
                .active(true)
                .build());
    }

    @Transactional
    public FinancialTransaction postInvestment(
            User user,
            InvestmentTransaction investmentTransaction,
            FinancialAccount account,
            boolean inflow,
            boolean countsAsInvestment) {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .user(user)
                .type(TransactionType.INVESTMENT)
                .amount(MoneyUtils.of(investmentTransaction.getAmount()))
                .transactionDate(investmentTransaction.getTransactionDate())
                .description(investmentTransaction.getType().name())
                .account(account)
                .investmentTransaction(investmentTransaction)
                .countsInMonthlyResult(countsAsInvestment)
                .affectsAccountBalance(account != null)
                .inflow(inflow)
                .active(true)
                .build();
        return persist(transaction);
    }

    @Transactional
    public FinancialTransaction postCardPayment(User user, FinancialAccount account, BigDecimal amount, LocalDate date, String description) {
        return persist(FinancialTransaction.builder()
                .user(user)
                .type(TransactionType.TRANSFER)
                .amount(MoneyUtils.of(amount))
                .transactionDate(date)
                .description(description)
                .account(account)
                .countsInMonthlyResult(false)
                .affectsAccountBalance(true)
                .inflow(false)
                .active(true)
                .build());
    }

    @Transactional
    public void reverse(FinancialTransaction transaction) {
        if (!transaction.isActive()) {
            return;
        }
        transaction.setActive(false);
        transactionRepository.save(transaction);
        applyBalance(transaction, true);
    }

    private FinancialTransaction persist(FinancialTransaction transaction) {
        FinancialTransaction saved = transactionRepository.save(transaction);
        applyBalance(saved, false);
        return saved;
    }

    private void applyBalance(FinancialTransaction transaction, boolean reversing) {
        if (!transaction.isAffectsAccountBalance() || transaction.getAccount() == null) {
            return;
        }
        FinancialAccount account = transaction.getAccount();
        BigDecimal delta = transaction.getAmount();
        boolean inflow = reversing != transaction.isInflow();
        if (inflow) {
            account.setCurrentBalance(account.getCurrentBalance().add(delta));
        } else {
            account.setCurrentBalance(account.getCurrentBalance().subtract(delta));
        }
        accountRepository.save(account);
    }
}
