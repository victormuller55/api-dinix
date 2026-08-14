package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateBudgetRequest;
import br.net.convertix.dinix.dto.request.UpdateBudgetRequest;
import br.net.convertix.dinix.dto.response.BudgetResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.BudgetService;
import br.net.convertix.dinix.web.Paginacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orcamentos")
@Tag(name = "Orçamentos")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public PageResponse<BudgetResponse> list(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano,
            Paginacao paginacao) {
        YearMonth now = YearMonth.now();
        return budgetService.list(
                SecurityUtils.currentUserId(),
                mes != null ? mes : now.getMonthValue(),
                ano != null ? ano : now.getYear(),
                paginacao.toPageable());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody CreateBudgetRequest request) {
        return budgetService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateBudgetRequest request) {
        return budgetService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        budgetService.delete(SecurityUtils.currentUserId(), id);
    }
}
