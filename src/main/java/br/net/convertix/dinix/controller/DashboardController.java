package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.response.DashboardResponse;
import br.net.convertix.dinix.dto.response.MonthlySummaryResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@Tag(name = "Painel e resumo")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/painel")
    public DashboardResponse dashboard(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth current = YearMonth.now();
        int resolvedMonth = mes != null ? mes : current.getMonthValue();
        int resolvedYear = ano != null ? ano : current.getYear();
        return dashboardService.dashboard(SecurityUtils.currentUserId(), resolvedMonth, resolvedYear);
    }

    @GetMapping("/api/v1/resumo/mensal")
    public MonthlySummaryResponse monthlySummary(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth current = YearMonth.now();
        int resolvedMonth = mes != null ? mes : current.getMonthValue();
        int resolvedYear = ano != null ? ano : current.getYear();
        return dashboardService.monthlySummary(SecurityUtils.currentUserId(), resolvedMonth, resolvedYear);
    }
}
