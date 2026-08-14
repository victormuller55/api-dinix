package br.net.convertix.dinix.dto.response;

import java.util.List;

public record CalendarResponse(
        List<CalendarDayResponse> days
) {
}
