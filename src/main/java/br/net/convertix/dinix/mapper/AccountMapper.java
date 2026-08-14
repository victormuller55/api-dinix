package br.net.convertix.dinix.mapper;

import br.net.convertix.dinix.dto.response.AccountResponse;
import br.net.convertix.dinix.entity.FinancialAccount;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(FinancialAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBankName(),
                account.getAccountType(),
                account.getInitialBalance(),
                account.getCurrentBalance(),
                account.getColor(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
