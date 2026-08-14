package br.net.convertix.dinix.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        UUID userId,
        String name,
        String email,
        String photoUrl,
        LocalDateTime expiresAt
) {
}
