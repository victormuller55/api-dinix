package br.net.convertix.dinix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class LoggingVerificationMailSender implements VerificationMailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingVerificationMailSender.class);

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        log.info("[test] Código {} enviado para {}", code, toEmail);
    }
}
