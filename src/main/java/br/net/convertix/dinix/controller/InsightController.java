package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.response.AlertResponse;
import br.net.convertix.dinix.dto.response.CalendarResponse;
import br.net.convertix.dinix.dto.response.ForecastResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.AlertService;
import br.net.convertix.dinix.service.CalendarService;
import br.net.convertix.dinix.service.ForecastService;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.net.convertix.dinix.web.Paginacao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@Tag(name = "Alertas, calendário e previsão")
public class InsightController {

    private final AlertService alertService;
    private final CalendarService calendarService;
    private final ForecastService forecastService;

    public InsightController(
            AlertService alertService,
            CalendarService calendarService,
            ForecastService forecastService) {
        this.alertService = alertService;
        this.calendarService = calendarService;
        this.forecastService = forecastService;
    }

    @GetMapping("/api/v1/alertas")
    public PageResponse<AlertResponse> alerts(Paginacao paginacao) {
        return alertService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @PatchMapping("/api/v1/alertas/{id}/lido")
    public AlertResponse markRead(@PathVariable UUID id) {
        return alertService.markRead(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/api/v1/calendario")
    public CalendarResponse calendar(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth now = YearMonth.now();
        return calendarService.calendar(
                SecurityUtils.currentUserId(),
                mes != null ? mes : now.getMonthValue(),
                ano != null ? ano : now.getYear());
    }

    @GetMapping("/api/v1/previsao")
    public ForecastResponse forecast(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth now = YearMonth.now().plusMonths(1);
        return forecastService.forecast(
                SecurityUtils.currentUserId(),
                mes != null ? mes : now.getMonthValue(),
                ano != null ? ano : now.getYear());
    }
}
