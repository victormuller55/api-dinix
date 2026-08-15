package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.request.PayCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.request.UpdateCreditCardInvoiceRequest;
import br.net.convertix.dinix.dto.response.CreditCardInvoiceResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.CreditCardInvoiceService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cartoes-de-credito")
@Tag(name = "Faturas de cartão")
public class CreditCardInvoiceController {

    private final CreditCardInvoiceService invoiceService;

    public CreditCardInvoiceController(CreditCardInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{creditCardId}/faturas")
    public List<CreditCardInvoiceResponse> list(@PathVariable UUID creditCardId) {
        return invoiceService.list(SecurityUtils.currentUserId(), creditCardId);
    }

    @PostMapping("/{creditCardId}/faturas")
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardInvoiceResponse create(
            @PathVariable UUID creditCardId,
            @Valid @RequestBody CreateCreditCardInvoiceRequest request) {
        return invoiceService.create(SecurityUtils.currentUserId(), creditCardId, request);
    }

    @PostMapping("/{creditCardId}/faturas/fechar-atual")
    public List<CreditCardInvoiceResponse> closeCurrent(@PathVariable UUID creditCardId) {
        return invoiceService.closeCurrent(SecurityUtils.currentUserId(), creditCardId);
    }

    @PutMapping("/faturas/{invoiceId}")
    public CreditCardInvoiceResponse update(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody UpdateCreditCardInvoiceRequest request) {
        return invoiceService.update(SecurityUtils.currentUserId(), invoiceId, request);
    }

    @PostMapping("/faturas/{invoiceId}/pagar")
    public CreditCardInvoiceResponse markPaid(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody PayCreditCardInvoiceRequest request) {
        return invoiceService.markPaid(SecurityUtils.currentUserId(), invoiceId, request);
    }

    @DeleteMapping("/faturas/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID invoiceId) {
        invoiceService.delete(SecurityUtils.currentUserId(), invoiceId);
    }
}
