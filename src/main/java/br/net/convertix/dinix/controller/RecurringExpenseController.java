package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateRecurringExpenseRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.RecurringExpenseResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.RecurringExpenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.net.convertix.dinix.web.Paginacao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/despesas-recorrentes")
@Tag(name = "Contas recorrentes")
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    public RecurringExpenseController(RecurringExpenseService recurringExpenseService) {
        this.recurringExpenseService = recurringExpenseService;
    }

    @GetMapping
    public PageResponse<RecurringExpenseResponse> list(Paginacao paginacao) {
        return recurringExpenseService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/{id}")
    public RecurringExpenseResponse get(@PathVariable UUID id) {
        return recurringExpenseService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringExpenseResponse create(@Valid @RequestBody CreateRecurringExpenseRequest request) {
        return recurringExpenseService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public RecurringExpenseResponse update(@PathVariable UUID id, @Valid @RequestBody CreateRecurringExpenseRequest request) {
        return recurringExpenseService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        recurringExpenseService.delete(SecurityUtils.currentUserId(), id);
    }
}
