package br.net.convertix.dinix.dto.response;

import java.util.UUID;

public record TagResponse(
        UUID id,
        String name
) {
}
