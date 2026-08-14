package br.net.convertix.dinix.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(
        int codeExpirationHours,
        int resendIntervalMinutes
) {
}
