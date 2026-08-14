package br.net.convertix.dinix.dto.response;

public record EmailVerifiedResponse(
        String email,
        boolean verified,
        String message
) {
}
