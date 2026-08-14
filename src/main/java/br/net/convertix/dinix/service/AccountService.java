package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateAccountRequest;
import br.net.convertix.dinix.dto.request.UpdateAccountRequest;
import br.net.convertix.dinix.dto.response.AccountResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.mapper.AccountMapper;
import br.net.convertix.dinix.repository.FinancialAccountRepository;
import br.net.convertix.dinix.util.BankCatalog;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final FinancialAccountRepository accountRepository;
    private final AuthService authService;
    private final AccountMapper accountMapper;

    public AccountService(
            FinancialAccountRepository accountRepository,
            AuthService authService,
            AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.authService = authService;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) {
        User user = authService.getActive(userId);
        String bankName = BankCatalog.displayName(request.bankName());
        FinancialAccount account = FinancialAccount.builder()
                .user(user)
                .name(bankName)
                .bankName(bankName)
                .accountType(request.accountType())
                .initialBalance(MoneyUtils.of(request.currentBalance()))
                .currentBalance(MoneyUtils.of(request.currentBalance()))
                .color(BankCatalog.color(request.bankName()))
                .active(true)
                .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(accountRepository.findByUserIdAndActiveTrue(userId, pageable)
                .map(accountMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID id) {
        return accountMapper.toResponse(getOwned(userId, id));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID id, UpdateAccountRequest request) {
        FinancialAccount account = getOwned(userId, id);
        String bankName = BankCatalog.displayName(request.bankName());
        account.setName(bankName);
        account.setBankName(bankName);
        account.setAccountType(request.accountType());
        account.setCurrentBalance(MoneyUtils.of(request.currentBalance()));
        account.setColor(BankCatalog.color(request.bankName()));
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        FinancialAccount account = getOwned(userId, id);
        account.setActive(false);
        accountRepository.save(account);
    }

    public FinancialAccount getOwned(UUID userId, UUID id) {
        return accountRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }
}
