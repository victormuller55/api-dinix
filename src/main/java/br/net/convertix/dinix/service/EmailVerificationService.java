package br.net.convertix.dinix.service;

import br.net.convertix.dinix.config.EmailVerificationProperties;
import br.net.convertix.dinix.dto.request.SendEmailCodeRequest;
import br.net.convertix.dinix.dto.request.VerifyEmailRequest;
import br.net.convertix.dinix.dto.response.EmailCodeSentResponse;
import br.net.convertix.dinix.dto.response.EmailVerifiedResponse;
import br.net.convertix.dinix.entity.EmailVerification;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ConflictException;
import br.net.convertix.dinix.repository.EmailVerificationRepository;
import br.net.convertix.dinix.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final VerificationMailSender mailSender;
    private final EmailVerificationProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationRepository verificationRepository,
            UserRepository userRepository,
            VerificationMailSender mailSender,
            EmailVerificationProperties properties) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Transactional
    public EmailCodeSentResponse sendCode(SendEmailCodeRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email);
        ensureResendAllowed(email);
        return createAndSendCode(email);
    }

    @Transactional
    public EmailCodeSentResponse sendPasswordResetCode(SendEmailCodeRequest request) {
        String email = normalizeEmail(request.email());
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(properties.codeExpirationHours());
        String message = "Se o e-mail existir na Dinix, enviamos um código. Ele expira em 3 horas.";

        if (!userRepository.existsByEmailIgnoreCase(email)) {
            return new EmailCodeSentResponse(message, expiresAt);
        }

        ensureResendAllowed(email);
        EmailCodeSentResponse sent = createAndSendCode(email, true);
        return new EmailCodeSentResponse(message, sent.expiresAt());
    }

    private EmailCodeSentResponse createAndSendCode(String email) {
        return createAndSendCode(email, false);
    }

    private EmailCodeSentResponse createAndSendCode(String email, boolean passwordReset) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(properties.codeExpirationHours());

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .verified(false)
                .build();
        verificationRepository.save(verification);

        if (passwordReset) {
            mailSender.sendPasswordResetCode(email, code);
        } else {
            mailSender.sendVerificationCode(email, code);
        }

        return new EmailCodeSentResponse(
                "Código enviado para o seu e-mail. Ele expira em 3 horas.",
                expiresAt
        );
    }

    @Transactional
    public EmailVerifiedResponse verify(VerifyEmailRequest request) {
        String email = normalizeEmail(request.email());
        EmailVerification verification = verificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException("Nenhum código encontrado para este e-mail"));

        if (verification.isVerified()) {
            if (!verification.getCode().equals(request.code())) {
                throw new BusinessException("Código inválido");
            }
            return new EmailVerifiedResponse(email, true, "E-mail já verificado");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Código expirado. Solicite um novo código.");
        }

        if (!verification.getCode().equals(request.code())) {
            throw new BusinessException("Código inválido");
        }

        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        return new EmailVerifiedResponse(email, true, "E-mail verificado com sucesso");
    }

    @Transactional(readOnly = true)
    public void ensureVerifiedForRegistration(String email) {
        boolean verified = verificationRepository
                .findTopByEmailIgnoreCaseAndVerifiedTrueOrderByVerifiedAtDesc(normalizeEmail(email))
                .isPresent();
        if (!verified) {
            throw new BusinessException("Verifique o e-mail antes de continuar");
        }
    }

    @Transactional
    public void consumeVerification(String email) {
        verificationRepository.findTopByEmailIgnoreCaseAndVerifiedTrueOrderByVerifiedAtDesc(normalizeEmail(email))
                .ifPresent(verification -> verificationRepository.delete(verification));
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Este e-mail já está cadastrado. Use outro endereço.");
        }
    }

    private void ensureResendAllowed(String email) {
        int intervaloMinutos = properties.resendIntervalMinutes();
        if (intervaloMinutos <= 0) {
            return;
        }

        verificationRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email).ifPresent(ultimo -> {
            LocalDateTime criadoEm = ultimo.getCreatedAt();
            if (criadoEm == null) {
                return;
            }
            LocalDateTime liberadoEm = criadoEm.plusMinutes(intervaloMinutos);
            LocalDateTime agora = LocalDateTime.now();
            if (agora.isBefore(liberadoEm)) {
                long segundosRestantes = Math.max(1, Duration.between(agora, liberadoEm).getSeconds());
                long minutosRestantes = Math.max(1, (segundosRestantes + 59) / 60);
                throw new BusinessException(
                        "Aguarde " + minutosRestantes + " min para solicitar um novo código.");
            }
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateCode() {
        int value = 100_000 + secureRandom.nextInt(900_000);
        return String.format("%06d", value);
    }
}
