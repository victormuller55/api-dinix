package br.net.convertix.dinix.mapper;

import br.net.convertix.dinix.dto.response.TransactionResponse;
import br.net.convertix.dinix.entity.FinancialTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(FinancialTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getAccount() != null ? transaction.getAccount().getId() : null,
                transaction.getCreditCard() != null ? transaction.getCreditCard().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getPurchase() != null ? transaction.getPurchase().getId() : null,
                transaction.getInstallment() != null ? transaction.getInstallment().getId() : null,
                transaction.getIncome() != null ? transaction.getIncome().getId() : null,
                transaction.getTransfer() != null ? transaction.getTransfer().getId() : null,
                transaction.isCountsInMonthlyResult(),
                transaction.isAffectsAccountBalance(),
                transaction.getCreatedAt()
        );
    }
}
