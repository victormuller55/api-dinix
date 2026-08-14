package br.net.convertix.dinix.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingPaymentResponse(
        String type,
        String description,
        BigDecimal amount,
        LocalDate dueDate
) {
}
