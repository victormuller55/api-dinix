package br.net.convertix.dinix.service;

import br.net.convertix.dinix.config.EmailVerificationProperties;
import br.net.convertix.dinix.config.MailProperties;
import br.net.convertix.dinix.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
@Profile("!test")
public class MailService implements VerificationMailSender {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final String LOGO_CONTENT_ID = "dinix-logo";

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final EmailVerificationProperties emailVerificationProperties;
    private final String mailPassword;
    private final String htmlTemplate;
    private final ClassPathResource logoResource;

    public MailService(
            JavaMailSender mailSender,
            MailProperties mailProperties,
            EmailVerificationProperties emailVerificationProperties,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.emailVerificationProperties = emailVerificationProperties;
        this.mailPassword = mailPassword;
        this.htmlTemplate = carregarTemplate();
        this.logoResource = new ClassPathResource("mail/logo.png");
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        enviar(
                toEmail,
                code,
                "Seu código de verificação — Dinix",
                "Use o código abaixo para verificar seu e-mail:",
                "Verificação de e-mail");
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        enviar(
                toEmail,
                code,
                "Redefinição de senha — Dinix",
                "Use o código abaixo para redefinir sua senha no Dinix:",
                "Redefinição de senha");
    }

    private void enviar(String toEmail, String code, String subject, String intro, String badge) {
        if (!StringUtils.hasText(mailPassword)) {
            log.warn(
                    "MAIL_PASSWORD vazio — e-mail NÃO enviado. Código para {}: {}",
                    toEmail,
                    code);
            return;
        }
        if (!StringUtils.hasText(mailProperties.from())) {
            throw new BusinessException(
                    "Envio de e-mail não configurado. Defina MAIL_FROM e as credenciais SMTP.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildHtml(code, intro, badge), true);
            helper.addInline(LOGO_CONTENT_ID, logoResource, "image/png");
            mailSender.send(message);
            log.info("E-mail enviado para {} ({})", toEmail, subject);
        } catch (MessagingException | MailException ex) {
            log.error("Falha ao enviar e-mail para {}: {}", toEmail, ex.getMessage(), ex);
            throw new BusinessException(
                    "Não foi possível enviar o e-mail. Verifique a configuração SMTP.");
        } catch (RuntimeException ex) {
            log.error("Erro inesperado ao enviar e-mail para {}", toEmail, ex);
            throw new BusinessException(
                    "Não foi possível enviar o e-mail. Tente novamente em instantes.");
        }
    }

    private String buildHtml(String code, String intro, String badge) {
        return htmlTemplate
                .replace("{{PREHEADER}}", escapeHtml(intro + " " + code))
                .replace("{{LOGO_SRC}}", "cid:" + LOGO_CONTENT_ID)
                .replace("{{BADGE}}", escapeHtml(badge))
                .replace("{{INTRO}}", escapeHtml(intro))
                .replace("{{CODE}}", escapeHtml(code))
                .replace(
                        "{{EXPIRATION_HOURS}}",
                        String.valueOf(emailVerificationProperties.codeExpirationHours()));
    }

    private static String carregarTemplate() {
        ClassPathResource resource = new ClassPathResource("mail/codigo.html");
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível carregar o template de e-mail", ex);
        }
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
