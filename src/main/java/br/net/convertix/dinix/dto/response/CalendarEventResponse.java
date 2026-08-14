package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.CalendarEventType;

import java.math.BigDecimal;

public record CalendarEventResponse(
        CalendarEventType type,
        String description,
        BigDecimal amount
) {
}
