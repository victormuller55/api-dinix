package br.net.convertix.dinix.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayResponse(
        LocalDate date,
        List<CalendarEventResponse> events
) {
}
