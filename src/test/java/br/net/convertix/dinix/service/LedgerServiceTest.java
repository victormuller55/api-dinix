package br.net.convertix.dinix.service;

import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.FinancialTransaction;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.repository.FinancialAccountRepository;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;
    @Mock
    private FinancialAccountRepository accountRepository;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(transactionRepository, accountRepository);
        lenient().when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void cashExpenseDecreasesAccountBalance() {
        User user = user();
        FinancialAccount account = account(user, new BigDecimal("1000.00"));
        ledgerService.postExpense(user, new BigDecimal("150.00"), LocalDate.of(2026, 8, 10),
                "Mercado", account, null, null, null, null, true);
        assertEquals(new BigDecimal("850.00"), account.getCurrentBalance());
    }

    @Test
    void creditCardExpenseDoesNotChangeAccountBalance() {
        User user = user();
        FinancialAccount account = account(user, new BigDecimal("1000.00"));
        CreditCard card = CreditCard.builder().user(user).name("Nubank").creditLimit(new BigDecimal("5000"))
                .closingDay(10).dueDay(20).active(true).build();
        ledgerService.postExpense(user, new BigDecimal("400.00"), LocalDate.of(2026, 8, 20),
                "Notebook 1/12", null, card, null, null, installment(), false);
        assertEquals(new BigDecimal("1000.00"), account.getCurrentBalance());
    }

    @Test
    void transferDoesNotCountInMonthlyResult() {
        User user = user();
        FinancialAccount source = account(user, new BigDecimal("1000.00"));
        FinancialAccount destination = account(user, new BigDecimal("100.00"));
        br.net.convertix.dinix.entity.Transfer transfer = br.net.convertix.dinix.entity.Transfer.builder()
                .user(user).sourceAccount(source).destinationAccount(destination)
                .amount(new BigDecimal("500.00")).transferDate(LocalDate.of(2026, 8, 13)).active(true).build();

        ledgerService.postTransfer(transfer);

        ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        captor.getAllValues().forEach(tx -> {
            assertEquals(TransactionType.TRANSFER, tx.getType());
            assertFalse(tx.isCountsInMonthlyResult());
        });
        assertEquals(new BigDecimal("500.00"), source.getCurrentBalance());
        assertEquals(new BigDecimal("600.00"), destination.getCurrentBalance());
    }

    @Test
    void installmentAmountsAreDistributedByMonthNotAsFullPurchase() {
        BigDecimal[] parts = MoneyUtils.splitInstallments(new BigDecimal("4800.00"), 12);
        assertEquals(12, parts.length);
        assertEquals(new BigDecimal("400.00"), parts[5]);
        BigDecimal august = parts[0];
        assertTrue(august.compareTo(new BigDecimal("4800.00")) < 0);
    }

    private User user() {
        User user = User.builder().name("Ana").email("ana@test.com").password("x").active(true).build();
        user.setId(UUID.randomUUID());
        return user;
    }

    private FinancialAccount account(User user, BigDecimal balance) {
        FinancialAccount account = FinancialAccount.builder()
                .user(user).name("Nubank").accountType(br.net.convertix.dinix.enums.AccountType.CHECKING)
                .initialBalance(balance).currentBalance(balance).active(true).build();
        account.setId(UUID.randomUUID());
        return account;
    }

    private Installment installment() {
        Purchase purchase = Purchase.builder()
                .description("Notebook")
                .purchaseDate(LocalDate.of(2026, 8, 1))
                .totalAmount(new BigDecimal("4800"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .numberOfInstallments(12)
                .active(true)
                .build();
        return Installment.builder()
                .purchase(purchase)
                .installmentNumber(1)
                .totalInstallments(12)
                .amount(new BigDecimal("400.00"))
                .dueDate(LocalDate.of(2026, 8, 20))
                .status(InstallmentStatus.PENDING)
                .build();
    }
}
