package br.net.convertix.dinix.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID transactionId,
        String fileName,
        String fileUrl,
        String contentType,
        LocalDateTime createdAt
) {
}
