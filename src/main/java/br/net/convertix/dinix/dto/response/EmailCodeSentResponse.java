package br.net.convertix.dinix.dto.response;

import java.time.LocalDateTime;

public record EmailCodeSentResponse(
        String message,
        LocalDateTime expiresAt
) {
}
