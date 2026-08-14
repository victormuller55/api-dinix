package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateInvestmentRequest;
import br.net.convertix.dinix.dto.request.CreateInvestmentTransactionRequest;
import br.net.convertix.dinix.dto.response.InvestmentResponse;
import br.net.convertix.dinix.dto.response.InvestmentTransactionResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.InvestmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.net.convertix.dinix.web.Paginacao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investimentos")
@Tag(name = "Investimentos")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    public PageResponse<InvestmentResponse> list(Paginacao paginacao) {
        return investmentService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/{id}")
    public InvestmentResponse get(@PathVariable UUID id) {
        return investmentService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse create(@Valid @RequestBody CreateInvestmentRequest request) {
        return investmentService.create(SecurityUtils.currentUserId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        investmentService.delete(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/{id}/transacoes")
    public PageResponse<InvestmentTransactionResponse> transactions(@PathVariable UUID id, Paginacao paginacao) {
        return investmentService.listTransactions(SecurityUtils.currentUserId(), id, paginacao.toPageable());
    }

    @PostMapping("/{id}/transacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentTransactionResponse addTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInvestmentTransactionRequest request) {
        return investmentService.addTransaction(SecurityUtils.currentUserId(), id, request);
    }
}
