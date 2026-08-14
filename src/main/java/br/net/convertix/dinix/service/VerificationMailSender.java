package br.net.convertix.dinix.service;

public interface VerificationMailSender {

    void sendVerificationCode(String toEmail, String code);

    default void sendPasswordResetCode(String toEmail, String code) {
        sendVerificationCode(toEmail, code);
    }
}
