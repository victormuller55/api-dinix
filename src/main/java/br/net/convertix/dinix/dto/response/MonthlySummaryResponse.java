package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        int month,
        int year,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal totalInvestments,
        BigDecimal totalTransfers,
        BigDecimal availableBalance,
        BigDecimal expensePercentage,
        BigDecimal investmentPercentage,
        PeriodMetricResponse expensesComparison,
        List<CategoryBreakdownResponse> expensesByCategory,
        List<NamedAmountResponse> topPurchaseLocations,
        List<ProductStatResponse> topProducts
) {
}
