package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateSubscriptionRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.SubscriptionResponse;
import br.net.convertix.dinix.dto.response.SubscriptionSummaryResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.SubscriptionService;
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
@RequestMapping("/api/v1/assinaturas")
@Tag(name = "Assinaturas")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public PageResponse<SubscriptionResponse> list(Paginacao paginacao) {
        return subscriptionService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/resumo")
    public SubscriptionSummaryResponse summary() {
        return subscriptionService.summary(SecurityUtils.currentUserId());
    }

    @GetMapping("/{id}")
    public SubscriptionResponse get(@PathVariable UUID id) {
        return subscriptionService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public SubscriptionResponse update(@PathVariable UUID id, @Valid @RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        subscriptionService.cancel(SecurityUtils.currentUserId(), id);
    }
}
