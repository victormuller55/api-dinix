package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;

public record PeriodMetricResponse(
        BigDecimal total,
        long count,
        BigDecimal previousTotal,
        BigDecimal variationPercent
) {
}
