package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.response.NamedAmountResponse;
import br.net.convertix.dinix.dto.response.NetWorthHistoryItemResponse;
import br.net.convertix.dinix.dto.response.NetWorthResponse;
import br.net.convertix.dinix.dto.response.ReportResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.Investment;
import br.net.convertix.dinix.entity.NetWorthSnapshot;
import br.net.convertix.dinix.enums.AccountType;
import br.net.convertix.dinix.enums.CreditCardInvoiceStatus;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.repository.CreditCardInvoiceRepository;
import br.net.convertix.dinix.repository.CreditCardRepository;
import br.net.convertix.dinix.repository.FinancialAccountRepository;
import br.net.convertix.dinix.repository.InvestmentRepository;
import br.net.convertix.dinix.repository.NetWorthSnapshotRepository;
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
public class ReportService {

    private final DashboardService dashboardService;
    private final FinancialAccountRepository accountRepository;
    private final InvestmentRepository investmentRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditCardInvoiceRepository invoiceRepository;
    private final NetWorthSnapshotRepository snapshotRepository;
    private final br.net.convertix.dinix.repository.FinancialTransactionRepository transactionRepository;
    private final AuthService authService;

    public ReportService(
            DashboardService dashboardService,
            FinancialAccountRepository accountRepository,
            InvestmentRepository investmentRepository,
            CreditCardRepository creditCardRepository,
            CreditCardInvoiceRepository invoiceRepository,
            NetWorthSnapshotRepository snapshotRepository,
            br.net.convertix.dinix.repository.FinancialTransactionRepository transactionRepository,
            AuthService authService) {
        this.dashboardService = dashboardService;
        this.accountRepository = accountRepository;
        this.investmentRepository = investmentRepository;
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public ReportResponse monthly(UUID userId, int month, int year) {
        LocalDate start = DateUtils.startOfMonth(month, year);
        LocalDate end = DateUtils.endOfMonth(month, year);
        BigDecimal expenses = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(userId, TransactionType.EXPENSE, start, end));
        long count = transactionRepository.countByTypeInPeriod(userId, TransactionType.EXPENSE, start, end);
        List<NamedAmountResponse> breakdown = dashboardService.expensesByCategory(userId, start, end).stream()
                .map(item -> new NamedAmountResponse(item.category(), item.amount()))
                .toList();
        return new ReportResponse("Relatório mensal", month, year, expenses, count, breakdown);
    }

    @Transactional(readOnly = true)
    public ReportResponse yearly(UUID userId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        BigDecimal expenses = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(userId, TransactionType.EXPENSE, start, end));
        long count = transactionRepository.countByTypeInPeriod(userId, TransactionType.EXPENSE, start, end);
        return new ReportResponse("Relatório anual", 0, year, expenses, count,
                dashboardService.expensesByCategory(userId, start, end).stream()
                        .map(item -> new NamedAmountResponse(item.category(), item.amount()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public ReportResponse byType(UUID userId, TransactionType type, int month, int year, String title) {
        LocalDate start = DateUtils.startOfMonth(month, year);
        LocalDate end = DateUtils.endOfMonth(month, year);
        BigDecimal total = MoneyUtils.of(transactionRepository.sumByTypeInPeriod(userId, type, start, end));
        long count = transactionRepository.countByTypeInPeriod(userId, type, start, end);
        return new ReportResponse(title, month, year, total, count, List.of());
    }

    @Transactional(readOnly = true)
    public ReportResponse categories(UUID userId, int month, int year) {
        LocalDate start = DateUtils.startOfMonth(month, year);
        LocalDate end = DateUtils.endOfMonth(month, year);
        List<NamedAmountResponse> breakdown = dashboardService.expensesByCategory(userId, start, end).stream()
                .map(item -> new NamedAmountResponse(item.category(), item.amount()))
                .toList();
        BigDecimal total = breakdown.stream().map(NamedAmountResponse::amount).reduce(MoneyUtils.zero(), BigDecimal::add);
        return new ReportResponse("Gastos por categoria", month, year, total, breakdown.size(), breakdown);
    }

    @Transactional
    public NetWorthResponse netWorth(UUID userId) {
        NetWorthResponse current = calculate(userId);
        YearMonth now = YearMonth.now();
        snapshotRepository.findByUserIdAndMonthAndYear(userId, now.getMonthValue(), now.getYear())
                .ifPresentOrElse(snapshot -> {
                    snapshot.setAccountsBalance(current.accountsBalance());
                    snapshot.setInvestmentsValue(current.investmentsValue());
                    snapshot.setDebts(current.debts());
                    snapshot.setNetWorth(current.netWorth());
                    snapshotRepository.save(snapshot);
                }, () -> snapshotRepository.save(NetWorthSnapshot.builder()
                        .user(authService.getActive(userId))
                        .month(now.getMonthValue())
                        .year(now.getYear())
                        .accountsBalance(current.accountsBalance())
                        .investmentsValue(current.investmentsValue())
                        .debts(current.debts())
                        .netWorth(current.netWorth())
                        .build()));
        return current;
    }

    @Transactional(readOnly = true)
    public List<NetWorthHistoryItemResponse> history(UUID userId) {
        return snapshotRepository.findByUserIdOrderByYearAscMonthAsc(userId).stream()
                .map(s -> new NetWorthHistoryItemResponse(
                        s.getMonth(), s.getYear(), s.getAccountsBalance(),
                        s.getInvestmentsValue(), s.getDebts(), s.getNetWorth()))
                .toList();
    }

    private NetWorthResponse calculate(UUID userId) {
        List<FinancialAccount> financialAccounts = accountRepository.findByUserIdAndActiveTrue(userId);
        BigDecimal accounts = financialAccounts.stream()
                .filter(a -> a.getAccountType() != AccountType.INVESTMENT)
                .map(FinancialAccount::getCurrentBalance)
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        BigDecimal accountInvestments = financialAccounts.stream()
                .filter(a -> a.getAccountType() == AccountType.INVESTMENT)
                .map(FinancialAccount::getCurrentBalance)
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        BigDecimal portfolioInvestments = investmentRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(Investment::getCurrentValue)
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        BigDecimal investments = accountInvestments.add(portfolioInvestments);
        BigDecimal debts = creditCardRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(card -> MoneyUtils.of(invoiceRepository.sumAmountByCardAndStatuses(
                        card.getId(),
                        List.of(
                                CreditCardInvoiceStatus.CURRENT,
                                CreditCardInvoiceStatus.UPCOMING,
                                CreditCardInvoiceStatus.CLOSED))))
                .reduce(MoneyUtils.zero(), BigDecimal::add);
        return new NetWorthResponse(accounts, investments, debts, accounts.add(investments).subtract(debts));
    }
}
