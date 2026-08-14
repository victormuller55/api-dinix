package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAttachmentRequest(
        @NotNull UUID transactionId,
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 1000) String fileUrl,
        @Size(max = 120) String contentType
) {
}
