package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.ChangePasswordRequest;
import br.net.convertix.dinix.dto.request.DeleteAccountRequest;
import br.net.convertix.dinix.dto.request.UpdateProfileRequest;
import br.net.convertix.dinix.dto.request.VerifyEmailRequest;
import br.net.convertix.dinix.dto.response.AuthResponse;
import br.net.convertix.dinix.dto.response.UserResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/eu")
    @Operation(summary = "Dados do usuário autenticado")
    public UserResponse me() {
        return authService.me(SecurityUtils.currentUserId());
    }

    @PatchMapping("/eu")
    @Operation(summary = "Atualizar nome do perfil")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/eu/senha")
    @Operation(summary = "Trocar senha")
    public UserResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/eu/email")
    @Operation(summary = "Trocar e-mail após verificação")
    public AuthResponse changeEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.changeEmail(SecurityUtils.currentUserId(), request);
    }

    @PostMapping("/eu/excluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar a conta")
    public void deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        authService.deleteAccount(SecurityUtils.currentUserId(), request);
    }

    @PutMapping(value = "/eu/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar foto de perfil")
    public UserResponse updatePhoto(@RequestPart("foto") MultipartFile foto) {
        return authService.updatePhoto(SecurityUtils.currentUserId(), foto);
    }

    @DeleteMapping("/eu/foto")
    @Operation(summary = "Remover foto de perfil")
    public UserResponse deletePhoto() {
        return authService.deletePhoto(SecurityUtils.currentUserId());
    }
}
