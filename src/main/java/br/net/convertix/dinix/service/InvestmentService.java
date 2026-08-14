package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateInvestmentRequest;
import br.net.convertix.dinix.dto.request.CreateInvestmentTransactionRequest;
import br.net.convertix.dinix.dto.response.InvestmentResponse;
import br.net.convertix.dinix.dto.response.InvestmentTransactionResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.Investment;
import br.net.convertix.dinix.entity.InvestmentTransaction;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.InvestmentTransactionType;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.InvestmentRepository;
import br.net.convertix.dinix.repository.InvestmentTransactionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentTransactionRepository transactionRepository;
    private final AuthService authService;
    private final AccountService accountService;
    private final LedgerService ledgerService;

    public InvestmentService(
            InvestmentRepository investmentRepository,
            InvestmentTransactionRepository transactionRepository,
            AuthService authService,
            AccountService accountService,
            LedgerService ledgerService) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public InvestmentResponse create(UUID userId, CreateInvestmentRequest request) {
        User user = authService.getActive(userId);
        Investment investment = Investment.builder()
                .user(user)
                .name(request.name())
                .institution(request.institution())
                .type(request.type())
                .ticker(request.ticker())
                .currentValue(MoneyUtils.zero())
                .quantity(BigDecimal.ZERO)
                .totalInvested(MoneyUtils.zero())
                .active(true)
                .build();
        return toResponse(investmentRepository.save(investment));
    }

    @Transactional(readOnly = true)
    public PageResponse<InvestmentResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(investmentRepository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public InvestmentResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Investment investment = getOwned(userId, id);
        investment.setActive(false);
        investmentRepository.save(investment);
    }

    @Transactional
    public InvestmentTransactionResponse addTransaction(UUID userId, UUID investmentId, CreateInvestmentTransactionRequest request) {
        Investment investment = getOwned(userId, investmentId);
        FinancialAccount account = request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null;
        InvestmentTransaction transaction = InvestmentTransaction.builder()
                .investment(investment)
                .account(account)
                .type(request.type())
                .amount(MoneyUtils.of(request.amount()))
                .quantity(request.quantity())
                .price(request.price())
                .transactionDate(request.transactionDate())
                .notes(request.notes())
                .build();
        InvestmentTransaction saved = transactionRepository.save(transaction);
        applyToInvestment(investment, saved);
        boolean inflow = request.type() == InvestmentTransactionType.SELL
                || request.type() == InvestmentTransactionType.WITHDRAW
                || request.type() == InvestmentTransactionType.DIVIDEND
                || request.type() == InvestmentTransactionType.INTEREST;
        boolean countsAsInvestment = request.type() == InvestmentTransactionType.BUY
                || request.type() == InvestmentTransactionType.DEPOSIT;
        ledgerService.postInvestment(investment.getUser(), saved, account, inflow, countsAsInvestment);
        return toTxResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvestmentTransactionResponse> listTransactions(UUID userId, UUID investmentId, Pageable pageable) {
        getOwned(userId, investmentId);
        return PageResponse.from(transactionRepository
                .findByInvestmentIdAndInvestmentUserId(investmentId, userId, pageable)
                .map(this::toTxResponse));
    }

    private void applyToInvestment(Investment investment, InvestmentTransaction tx) {
        BigDecimal qty = tx.getQuantity() != null ? tx.getQuantity() : BigDecimal.ZERO;
        switch (tx.getType()) {
            case BUY, DEPOSIT -> {
                investment.setTotalInvested(investment.getTotalInvested().add(tx.getAmount()));
                investment.setQuantity(investment.getQuantity().add(qty));
                investment.setCurrentValue(investment.getCurrentValue().add(tx.getAmount()));
            }
            case SELL, WITHDRAW -> {
                investment.setQuantity(investment.getQuantity().subtract(qty));
                investment.setCurrentValue(investment.getCurrentValue().subtract(tx.getAmount()).max(BigDecimal.ZERO));
            }
            case DIVIDEND, INTEREST -> {
                // rendimento não aumenta o principal investido
            }
        }
        investmentRepository.save(investment);
    }

    public Investment getOwned(UUID userId, UUID id) {
        return investmentRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento não encontrado"));
    }

    private InvestmentResponse toResponse(Investment investment) {
        BigDecimal profit = investment.getCurrentValue().subtract(investment.getTotalInvested());
        BigDecimal profitability = MoneyUtils.percentage(profit, investment.getTotalInvested());
        return new InvestmentResponse(
                investment.getId(), investment.getName(), investment.getInstitution(), investment.getType(),
                investment.getTicker(), investment.getCurrentValue(), investment.getQuantity(),
                investment.getTotalInvested(), profit, profitability,
                investment.getCreatedAt(), investment.getUpdatedAt());
    }

    private InvestmentTransactionResponse toTxResponse(InvestmentTransaction tx) {
        return new InvestmentTransactionResponse(
                tx.getId(), tx.getInvestment().getId(), tx.getType(), tx.getAmount(), tx.getQuantity(),
                tx.getPrice(), tx.getAccount() != null ? tx.getAccount().getId() : null,
                tx.getTransactionDate(), tx.getNotes());
    }
}
