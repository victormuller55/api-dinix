package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateCreditCardRequest;
import br.net.convertix.dinix.dto.request.UpdateCreditCardRequest;
import br.net.convertix.dinix.dto.response.CreditCardResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.CreditCardService;
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
@RequestMapping("/api/v1/cartoes-de-credito")
@Tag(name = "Cartões")
public class CreditCardController {

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @GetMapping
    public PageResponse<CreditCardResponse> list(Paginacao paginacao) {
        return creditCardService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/{id}")
    public CreditCardResponse get(@PathVariable UUID id) {
        return creditCardService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardResponse create(@Valid @RequestBody CreateCreditCardRequest request) {
        return creditCardService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public CreditCardResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCreditCardRequest request) {
        return creditCardService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        creditCardService.delete(SecurityUtils.currentUserId(), id);
    }
}
