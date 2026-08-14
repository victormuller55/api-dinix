package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateTransferRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.TransferResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.Transfer;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.TransferRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AuthService authService;
    private final AccountService accountService;
    private final LedgerService ledgerService;

    public TransferService(
            TransferRepository transferRepository,
            FinancialTransactionRepository transactionRepository,
            AuthService authService,
            AccountService accountService,
            LedgerService ledgerService) {
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public TransferResponse create(UUID userId, CreateTransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new BusinessException("Conta de origem e destino devem ser diferentes");
        }
        User user = authService.getActive(userId);
        FinancialAccount source = accountService.getOwned(userId, request.sourceAccountId());
        FinancialAccount destination = accountService.getOwned(userId, request.destinationAccountId());
        Transfer transfer = Transfer.builder()
                .user(user)
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(MoneyUtils.of(request.amount()))
                .transferDate(request.transferDate())
                .description(request.description())
                .active(true)
                .build();
        Transfer saved = transferRepository.save(transfer);
        ledgerService.postTransfer(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(transferRepository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transfer transfer = transferRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada"));
        transfer.setActive(false);
        transactionRepository.findByTransferIdAndActiveTrue(transfer.getId()).forEach(ledgerService::reverse);
        transferRepository.save(transfer);
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                transfer.getTransferDate(),
                transfer.getDescription(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }
}
