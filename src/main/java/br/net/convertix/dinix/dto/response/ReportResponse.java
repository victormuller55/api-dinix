package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportResponse(
        String title,
        int month,
        Integer year,
        BigDecimal total,
        long count,
        List<NamedAmountResponse> breakdown
) {
}
