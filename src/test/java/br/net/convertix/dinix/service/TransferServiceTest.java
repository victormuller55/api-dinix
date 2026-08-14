package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateTransferRequest;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.AccountType;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.TransferRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private FinancialTransactionRepository transactionRepository;
    @Mock
    private AuthService authService;
    @Mock
    private AccountService accountService;
    @Mock
    private LedgerService ledgerService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                transferRepository, transactionRepository, authService, accountService, ledgerService);
    }

    @Test
    void createPostsTransferAndNeverAnExpense() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().name("Ana").email("ana@test.com").password("x").active(true).build();
        user.setId(userId);
        FinancialAccount source = FinancialAccount.builder()
                .user(user).name("Nubank").accountType(AccountType.CHECKING)
                .initialBalance(new BigDecimal("1000")).currentBalance(new BigDecimal("1000")).active(true).build();
        source.setId(UUID.randomUUID());
        FinancialAccount destination = FinancialAccount.builder()
                .user(user).name("C6").accountType(AccountType.CHECKING)
                .initialBalance(new BigDecimal("100")).currentBalance(new BigDecimal("100")).active(true).build();
        destination.setId(UUID.randomUUID());

        when(authService.getActive(userId)).thenReturn(user);
        when(accountService.getOwned(userId, source.getId())).thenReturn(source);
        when(accountService.getOwned(userId, destination.getId())).thenReturn(destination);
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.create(userId, new CreateTransferRequest(
                source.getId(), destination.getId(), new BigDecimal("500"), LocalDate.of(2026, 8, 13), "entre contas"));

        ArgumentCaptor<br.net.convertix.dinix.entity.Transfer> captor =
                ArgumentCaptor.forClass(br.net.convertix.dinix.entity.Transfer.class);
        verify(ledgerService).postTransfer(captor.capture());
        assertEquals(new BigDecimal("500.00"), captor.getValue().getAmount());
    }
}
