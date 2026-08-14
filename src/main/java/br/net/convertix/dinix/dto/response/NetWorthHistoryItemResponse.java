package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record NetWorthHistoryItemResponse(
        int month,
        int year,
        BigDecimal accountsBalance,
        BigDecimal investmentsValue,
        BigDecimal debts,
        BigDecimal netWorth
) {
}
