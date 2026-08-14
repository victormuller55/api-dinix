package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.ChangePasswordRequest;
import br.net.convertix.dinix.dto.request.DeleteAccountRequest;
import br.net.convertix.dinix.dto.request.LoginRequest;
import br.net.convertix.dinix.dto.request.RegisterRequest;
import br.net.convertix.dinix.dto.request.ResetPasswordRequest;
import br.net.convertix.dinix.dto.request.SendEmailCodeRequest;
import br.net.convertix.dinix.dto.request.UpdateProfileRequest;
import br.net.convertix.dinix.dto.request.VerifyEmailRequest;
import br.net.convertix.dinix.dto.response.AuthResponse;
import br.net.convertix.dinix.dto.response.EmailCodeSentResponse;
import br.net.convertix.dinix.dto.response.EmailVerifiedResponse;
import br.net.convertix.dinix.dto.response.UserResponse;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ConflictException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.UserRepository;
import br.net.convertix.dinix.security.JwtProperties;
import br.net.convertix.dinix.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final DefaultCategoryService defaultCategoryService;
    private final EmailVerificationService emailVerificationService;
    private final ProfilePhotoStorage profilePhotoStorage;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            AuthenticationManager authenticationManager,
            DefaultCategoryService defaultCategoryService,
            EmailVerificationService emailVerificationService,
            ProfilePhotoStorage profilePhotoStorage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.authenticationManager = authenticationManager;
        this.defaultCategoryService = defaultCategoryService;
        this.emailVerificationService = emailVerificationService;
        this.profilePhotoStorage = profilePhotoStorage;
    }

    public EmailCodeSentResponse sendEmailCode(SendEmailCodeRequest request) {
        return emailVerificationService.sendCode(request);
    }

    public EmailVerifiedResponse verifyEmail(VerifyEmailRequest request) {
        return emailVerificationService.verify(request);
    }

    public EmailCodeSentResponse sendPasswordResetCode(SendEmailCodeRequest request) {
        return emailVerificationService.sendPasswordResetCode(request);
    }

    @Transactional
    public EmailVerifiedResponse resetPassword(ResetPasswordRequest request) {
        EmailVerifiedResponse verified = emailVerificationService.verify(
                new VerifyEmailRequest(request.email(), request.code()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (!user.isActive()) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        emailVerificationService.consumeVerification(request.email());
        return new EmailVerifiedResponse(
                verified.email(),
                true,
                "Senha redefinida com sucesso");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        emailVerificationService.ensureVerifiedForRegistration(request.email());
        User user = userRepository.save(User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .active(true)
                .build());
        defaultCategoryService.seedFor(user);
        emailVerificationService.consumeVerification(request.email());
        return toAuth(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toAuth(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return toUser(getActive(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getActive(userId);
        user.setName(request.name().trim());
        return toUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = getActive(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        return toUser(userRepository.save(user));
    }

    @Transactional
    public AuthResponse changeEmail(UUID userId, VerifyEmailRequest request) {
        User user = getActive(userId);
        String email = request.email().trim().toLowerCase();
        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("Informe um e-mail diferente do atual");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        emailVerificationService.verify(request);
        emailVerificationService.ensureVerifiedForRegistration(email);
        user.setEmail(email);
        User saved = userRepository.save(user);
        emailVerificationService.consumeVerification(email);
        return toAuth(saved);
    }

    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        User user = getActive(userId);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Senha incorreta");
        }
        user.setActive(false);
        user.setName("Conta encerrada");
        user.setEmail("deleted+" + user.getId() + "@deleted.local");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        profilePhotoStorage.deleteByUrl(user.getPhotoUrl());
        user.setPhotoUrl(null);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updatePhoto(UUID userId, MultipartFile foto) {
        User user = getActive(userId);
        String anterior = user.getPhotoUrl();
        String url = profilePhotoStorage.store(userId, foto);
        user.setPhotoUrl(url);
        User salvo = userRepository.save(user);
        profilePhotoStorage.deleteByUrl(anterior);
        return toUser(salvo);
    }

    @Transactional
    public UserResponse deletePhoto(UUID userId) {
        User user = getActive(userId);
        profilePhotoStorage.deleteByUrl(user.getPhotoUrl());
        user.setPhotoUrl(null);
        return toUser(userRepository.save(user));
    }

    public User getActive(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (!user.isActive()) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return user;
    }

    private UserResponse toUser(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getPhotoUrl(), user.isActive(),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    private AuthResponse toAuth(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhotoUrl(),
                LocalDateTime.now().plusSeconds(jwtProperties.expirationMs() / 1000)
        );
    }
}
