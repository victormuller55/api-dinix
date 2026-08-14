package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreatePurchaseRequest;
import br.net.convertix.dinix.dto.request.UpdatePurchaseRequest;
import br.net.convertix.dinix.dto.response.InstallmentResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.PurchaseResponse;
import br.net.convertix.dinix.mapper.PurchaseMapper;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.PurchaseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;

    public PurchaseController(PurchaseService purchaseService, PurchaseMapper purchaseMapper) {
        this.purchaseService = purchaseService;
        this.purchaseMapper = purchaseMapper;
    }

    @GetMapping
    public PageResponse<PurchaseResponse> list(
            Paginacao paginacao,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(name = "data_inicio", required = false) LocalDate dataInicio,
            @RequestParam(name = "data_fim", required = false) LocalDate dataFim,
            @RequestParam(required = false) List<LocalDate> dias) {
        return purchaseService.list(
                SecurityUtils.currentUserId(),
                paginacao.toPageable(),
                mes,
                ano,
                dataInicio,
                dataFim,
                dias);
    }

    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable UUID id) {
        return purchaseService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody CreatePurchaseRequest request) {
        return purchaseService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public PurchaseResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePurchaseRequest request) {
        return purchaseService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        purchaseService.delete(SecurityUtils.currentUserId(), id);
    }

    @PostMapping("/parcelas/{installmentId}/pagar")
    public InstallmentResponse pay(@PathVariable UUID installmentId) {
        return purchaseMapper.toInstallment(
                purchaseService.payInstallment(SecurityUtils.currentUserId(), installmentId));
    }
}
