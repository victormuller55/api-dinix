package br.net.convertix.dinix.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email) {
}
