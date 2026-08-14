package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        int month,
        int year,
        PeriodMetricResponse income,
        PeriodMetricResponse expenses,
        BigDecimal investments,
        BigDecimal available,
        List<CategoryBreakdownResponse> expensesByCategory,
        List<UpcomingPaymentResponse> upcomingPayments,
        List<CreditCardResponse> creditCards,
        List<SubscriptionResponse> subscriptions
) {
}
