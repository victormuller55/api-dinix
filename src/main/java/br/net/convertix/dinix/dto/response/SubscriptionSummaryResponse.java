package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SubscriptionSummaryResponse(
        BigDecimal monthlyTotal,
        BigDecimal yearlyTotal,
        List<NextPaymentItemResponse> nextPayments
) {
}
