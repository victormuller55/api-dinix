package br.net.convertix.dinix.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String photoUrl,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
