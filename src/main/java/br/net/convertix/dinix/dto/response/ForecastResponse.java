package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record ForecastResponse(
        int month,
        int year,
        BigDecimal expectedIncome,
        BigDecimal committedExpenses,
        BigDecimal expectedInvestments,
        BigDecimal expectedAvailable
) {
}
