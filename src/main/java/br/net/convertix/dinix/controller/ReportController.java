package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.response.NamedAmountResponse;
import br.net.convertix.dinix.dto.response.NetWorthHistoryItemResponse;
import br.net.convertix.dinix.dto.response.NetWorthResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.ProductStatResponse;
import br.net.convertix.dinix.dto.response.ReportResponse;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.DashboardService;
import br.net.convertix.dinix.service.ReportService;
import br.net.convertix.dinix.util.DateUtils;
import br.net.convertix.dinix.web.Paginacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "Relatórios e estatísticas")
public class ReportController {

    private final ReportService reportService;
    private final DashboardService dashboardService;

    public ReportController(ReportService reportService, DashboardService dashboardService) {
        this.reportService = reportService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/relatorios/mensal")
    public ReportResponse monthly(@RequestParam(name = "mes") int mes, @RequestParam(name = "ano") int ano) {
        return reportService.monthly(SecurityUtils.currentUserId(), mes, ano);
    }

    @GetMapping("/api/v1/relatorios/anual")
    public ReportResponse yearly(@RequestParam(name = "ano") int ano) {
        return reportService.yearly(SecurityUtils.currentUserId(), ano);
    }

    @GetMapping("/api/v1/relatorios/categorias")
    public ReportResponse categories(@RequestParam(name = "mes") int mes, @RequestParam(name = "ano") int ano) {
        return reportService.categories(SecurityUtils.currentUserId(), mes, ano);
    }

    @GetMapping("/api/v1/relatorios/receitas")
    public ReportResponse income(@RequestParam(name = "mes") int mes, @RequestParam(name = "ano") int ano) {
        return reportService.byType(SecurityUtils.currentUserId(), TransactionType.INCOME, mes, ano, "Receitas");
    }

    @GetMapping("/api/v1/relatorios/despesas")
    public ReportResponse expenses(@RequestParam(name = "mes") int mes, @RequestParam(name = "ano") int ano) {
        return reportService.byType(SecurityUtils.currentUserId(), TransactionType.EXPENSE, mes, ano, "Despesas");
    }

    @GetMapping("/api/v1/relatorios/investimentos")
    public ReportResponse investments(@RequestParam(name = "mes") int mes, @RequestParam(name = "ano") int ano) {
        return reportService.byType(SecurityUtils.currentUserId(), TransactionType.INVESTMENT, mes, ano, "Investimentos");
    }

    @GetMapping("/api/v1/relatorios/patrimonio")
    public NetWorthResponse reportNetWorth() {
        return reportService.netWorth(SecurityUtils.currentUserId());
    }

    @GetMapping("/api/v1/patrimonio")
    public NetWorthResponse netWorth() {
        return reportService.netWorth(SecurityUtils.currentUserId());
    }

    @GetMapping("/api/v1/patrimonio/historico")
    public PageResponse<NetWorthHistoryItemResponse> history(Paginacao paginacao) {
        return PageResponse.fromList(reportService.history(SecurityUtils.currentUserId()), paginacao.toPageable());
    }

    @GetMapping("/api/v1/estatisticas/locais")
    public List<NamedAmountResponse> locations(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth current = resolve(mes, ano);
        return dashboardService.topLocations(
                SecurityUtils.currentUserId(),
                DateUtils.startOfMonth(current.getMonthValue(), current.getYear()),
                DateUtils.endOfMonth(current.getMonthValue(), current.getYear()));
    }

    @GetMapping("/api/v1/estatisticas/produtos")
    public List<ProductStatResponse> products(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano) {
        YearMonth current = resolve(mes, ano);
        return dashboardService.topProducts(
                SecurityUtils.currentUserId(),
                DateUtils.startOfMonth(current.getMonthValue(), current.getYear()),
                DateUtils.endOfMonth(current.getMonthValue(), current.getYear()));
    }

    private YearMonth resolve(Integer mes, Integer ano) {
        YearMonth now = YearMonth.now();
        return YearMonth.of(ano != null ? ano : now.getYear(), mes != null ? mes : now.getMonthValue());
    }
}
