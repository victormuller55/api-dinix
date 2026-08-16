package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateAttachmentRequest;
import br.net.convertix.dinix.dto.request.CreateTagRequest;
import br.net.convertix.dinix.dto.response.AttachmentResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.TagResponse;
import br.net.convertix.dinix.dto.response.TransactionResponse;
import br.net.convertix.dinix.enums.TransactionType;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.net.convertix.dinix.web.Paginacao;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@Tag(name = "Transações, etiquetas e comprovantes")
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    public TransactionController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/api/v1/transacoes/busca")
    public PageResponse<TransactionResponse> search(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "tipo", required = false) TransactionType tipo,
            @RequestParam(name = "id_categoria", required = false) UUID idCategoria,
            @RequestParam(name = "id_conta", required = false) UUID idConta,
            @RequestParam(name = "id_cartao_credito", required = false) UUID idCartaoCredito,
            @RequestParam(name = "data_inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(name = "data_fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(name = "valor_min", required = false) BigDecimal valorMin,
            @RequestParam(name = "valor_max", required = false) BigDecimal valorMax,
            @RequestParam(name = "altera_saldo_conta", required = false) Boolean alteraSaldoConta,
            Paginacao paginacao) {
        return transactionQueryService.search(
                SecurityUtils.currentUserId(), busca, tipo, idCategoria, idConta, idCartaoCredito,
                dataInicio, dataFim, valorMin, valorMax, alteraSaldoConta, paginacao.toPageable());
    }

    @GetMapping("/api/v1/etiquetas")
    public PageResponse<TagResponse> tags(Paginacao paginacao) {
        return transactionQueryService.listTags(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @PostMapping("/api/v1/etiquetas")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@Valid @RequestBody CreateTagRequest request) {
        return transactionQueryService.createTag(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/api/v1/anexos")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse createAttachment(@Valid @RequestBody CreateAttachmentRequest request) {
        return transactionQueryService.createAttachment(SecurityUtils.currentUserId(), request);
    }

    @GetMapping("/api/v1/anexos")
    public PageResponse<AttachmentResponse> attachments(
            @RequestParam(name = "id_transacao") UUID idTransacao,
            Paginacao paginacao) {
        return transactionQueryService.listAttachments(SecurityUtils.currentUserId(), idTransacao, paginacao.toPageable());
    }
}
