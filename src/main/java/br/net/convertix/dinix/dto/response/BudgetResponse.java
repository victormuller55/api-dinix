package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        BigDecimal amountLimit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal usedPercentage,
        Integer month,
        Integer year
) {
}
