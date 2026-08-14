package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.LoginRequest;
import br.net.convertix.dinix.dto.request.RegisterRequest;
import br.net.convertix.dinix.dto.request.ResetPasswordRequest;
import br.net.convertix.dinix.dto.request.SendEmailCodeRequest;
import br.net.convertix.dinix.dto.request.VerifyEmailRequest;
import br.net.convertix.dinix.dto.response.AuthResponse;
import br.net.convertix.dinix.dto.response.EmailCodeSentResponse;
import br.net.convertix.dinix.dto.response.EmailVerifiedResponse;
import br.net.convertix.dinix.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/autenticacao")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuário")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/entrar")
    @Operation(summary = "Login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/enviar-codigo-email")
    @Operation(summary = "Enviar código de verificação por e-mail")
    public EmailCodeSentResponse sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        return authService.sendEmailCode(request);
    }

    @PostMapping("/verificar-email")
    @Operation(summary = "Verificar código de e-mail")
    public EmailVerifiedResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/esqueci-senha/enviar-codigo")
    @Operation(summary = "Enviar código para redefinir senha")
    public EmailCodeSentResponse sendPasswordResetCode(@Valid @RequestBody SendEmailCodeRequest request) {
        return authService.sendPasswordResetCode(request);
    }

    @PostMapping("/esqueci-senha/redefinir")
    @Operation(summary = "Redefinir senha com código de e-mail")
    public EmailVerifiedResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
