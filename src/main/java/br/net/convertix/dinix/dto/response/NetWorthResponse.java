package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record NetWorthResponse(
        BigDecimal accountsBalance,
        BigDecimal investmentsValue,
        BigDecimal debts,
        BigDecimal netWorth
) {
}
