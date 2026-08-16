package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateRecurringIncomeRequest;
import br.net.convertix.dinix.dto.request.ReceiveRecurringIncomeRequest;
import br.net.convertix.dinix.dto.response.IncomeResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.RecurringIncomeResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.RecurringIncomeService;
import br.net.convertix.dinix.web.Paginacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receitas-mensais")
@Tag(name = "Recebimentos mensais")
public class RecurringIncomeController {

    private final RecurringIncomeService recurringIncomeService;

    public RecurringIncomeController(RecurringIncomeService recurringIncomeService) {
        this.recurringIncomeService = recurringIncomeService;
    }

    @GetMapping
    public PageResponse<RecurringIncomeResponse> list(Paginacao paginacao) {
        return recurringIncomeService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/pendentes")
    public List<RecurringIncomeResponse> pending(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return recurringIncomeService.pendingOn(SecurityUtils.currentUserId(), data);
    }

    @GetMapping("/{id}")
    public RecurringIncomeResponse get(@PathVariable UUID id) {
        return recurringIncomeService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringIncomeResponse create(@Valid @RequestBody CreateRecurringIncomeRequest request) {
        return recurringIncomeService.create(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/{id}/receber")
    public IncomeResponse receive(
            @PathVariable UUID id,
            @Valid @RequestBody ReceiveRecurringIncomeRequest request,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return recurringIncomeService.receive(
                SecurityUtils.currentUserId(),
                id,
                request,
                data != null ? data : LocalDate.now());
    }

    @PutMapping("/{id}")
    public RecurringIncomeResponse update(
            @PathVariable UUID id, @Valid @RequestBody CreateRecurringIncomeRequest request) {
        return recurringIncomeService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        recurringIncomeService.delete(SecurityUtils.currentUserId(), id);
    }
}
