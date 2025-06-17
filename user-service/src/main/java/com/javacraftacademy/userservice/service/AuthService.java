package com.javacraftacademy.userservice.service;

import com.javacraftacademy.userservice.exception.EmailAlreadyExistsException;
import com.javacraftacademy.userservice.exception.InvalidCredentialsException;
import com.javacraftacademy.userservice.exception.TokenExpiredException;
import com.javacraftacademy.userservice.exception.UserNotFoundException;
import com.javacraftacademy.userservice.model.dto.request.LoginRequest;
import com.javacraftacademy.userservice.model.dto.request.PasswordResetRequest;
import com.javacraftacademy.userservice.model.dto.request.RegisterRequest;
import com.javacraftacademy.userservice.model.dto.response.AuthResponse;
import com.javacraftacademy.userservice.model.entity.RefreshToken;
import com.javacraftacademy.userservice.model.entity.Role;
import com.javacraftacademy.userservice.model.entity.User;
import com.javacraftacademy.userservice.repository.UserRepository;
import com.javacraftacademy.userservice.repository.RoleRepository;
import com.javacraftacademy.userservice.security.UserPrincipal;
import com.javacraftacademy.userservice.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    // Configuration du super admin via application.properties
    @Value("${app.super-admin.email:founder@javacraftacademy.com}")
    private String superAdminEmail;

    @Value("${app.super-admin.password:SuperSecretFounder2024!}")
    private String superAdminPassword;

    @Value("${app.super-admin.role:SUPER_ADMIN}")
    private String superAdminRole;

    // Constante pour l'ID du super admin
    private static final String SUPER_ADMIN_ID = "super-admin-001";
    private static final String SUPER_ADMIN_USERNAME = "founder";
    private static final String SUPER_ADMIN_FIRST_NAME = "Fondateur";
    private static final String SUPER_ADMIN_LAST_NAME = "JavaCraft";

    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new EmailAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Créer un nouvel utilisateur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isEmailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Assigner le rôle par défaut
        Role userRole = roleRepository.findByName(Constants.DEFAULT_ROLE)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", savedUser.getEmail(), e);
        }

        // Générer les tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getEmail());

        // Vérification du super admin avant l'authentification normale
        if (isSuperAdmin(request.getEmail(), request.getPassword())) {
            log.info("Super admin login detected: {}", request.getEmail());
            return createSuperAdminSession();
        }

        try {
            // Authentifier l'utilisateur normal
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // Vérifier si le compte est actif
            if (!user.getIsActive()) {
                log.warn("Login failed: Account is deactivated - {}", request.getEmail());
                throw new InvalidCredentialsException("Account is deactivated");
            }

            // Mettre à jour la dernière connexion
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Générer les tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(Role::getName)
                            .toList())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", request.getEmail(), e.getMessage());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Vérifie si les identifiants correspondent au super admin
     */
    private boolean isSuperAdmin(String email, String password) {
        return superAdminEmail.equals(email) && superAdminPassword.equals(password);
    }

    /**
     * Crée une session pour le super admin
     */
    private AuthResponse createSuperAdminSession() {
        log.info("Creating super admin session");

        // Créer un utilisateur virtuel pour le super admin
        User superAdminUser = createSuperAdminUser();

        // Générer des tokens avec une durée de vie prolongée
        String accessToken = jwtService.generateSuperAdminAccessToken(superAdminUser);
        String refreshToken = jwtService.generateSuperAdminRefreshToken(superAdminUser);

        log.info("Super admin session created successfully");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getSuperAdminTokenExpiration()) // 30 jours
                .userId(SUPER_ADMIN_ID)
                .username(SUPER_ADMIN_USERNAME)
                .email(superAdminEmail)
                .roles(List.of(superAdminRole))
                .isFounder(true) // Indicateur spécial pour le fondateur
                .build();
    }

    /**
     * Crée un objet User virtuel pour le super admin
     */
    private User createSuperAdminUser() {
        return User.builder()
                .id(SUPER_ADMIN_ID)
                .username(SUPER_ADMIN_USERNAME)
                .email(superAdminEmail)
                .firstName(SUPER_ADMIN_FIRST_NAME)
                .lastName(SUPER_ADMIN_LAST_NAME)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .roles(Set.of(createSuperAdminRole()))
                .build();
    }

    /**
     * Crée un rôle virtuel pour le super admin
     */
    private Role createSuperAdminRole() {
        return Role.builder()
                .name(superAdminRole)
                .description("Super Administrator with all permissions")
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Attempting to refresh token");

        if (!jwtService.validateRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        String userEmail = jwtService.getEmailFromRefreshToken(refreshToken);
        
        // Gérer le cas du super admin
        if (superAdminEmail.equals(userEmail)) {
            log.info("Refreshing super admin token");
            return createSuperAdminSession();
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Vérifier si le token de rafraîchissement existe
        RefreshToken storedToken = jwtService.getRefreshTokenEntity(refreshToken);
        if (storedToken == null || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expired or not found for user: {}", userEmail);
            throw new TokenExpiredException("Refresh token expired");
        }

        // Générer de nouveaux tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Supprimer l'ancien token de rafraîchissement
        jwtService.deleteRefreshToken(refreshToken);

        log.info("Tokens refreshed successfully for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .build();
    }

    public void logout(String refreshToken) {
        log.info("User logout requested");
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // Pour le super admin, pas besoin de supprimer de token en base
            String userEmail = jwtService.getEmailFromRefreshToken(refreshToken);
            if (!superAdminEmail.equals(userEmail)) {
                jwtService.deleteRefreshToken(refreshToken);
            }
            log.info("Logout completed successfully");
        }
    }

    public void resetPassword(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        // Le super admin ne peut pas réinitialiser son mot de passe via cette méthode
        if (superAdminEmail.equals(request.getEmail())) {
            log.warn("Password reset attempted for super admin email");
            throw new InvalidCredentialsException("Super admin password cannot be reset via this method");
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
            return;
        }

        User user = userOptional.get();
        
        // Générer un token de réinitialisation
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // Expire dans 1 heure
        
        userRepository.save(user);

        // Envoyer l'email de réinitialisation
        try {
            emailService.sendPasswordResetEmail(user, resetToken);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void confirmPasswordReset(String token, String newPassword) {
        log.info("Password reset confirmation requested");

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            log.warn("Password reset token expired for user: {}", user.getEmail());
            throw new TokenExpiredException("Password reset token expired");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Invalider tous les tokens de rafraîchissement existants
        jwtService.deleteAllRefreshTokensForUser(user.getId());

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    public void verifyEmail(String token) {
        log.info("Email verification requested");

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid verification token"));

        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    public void resendVerificationEmail(String email) {
        log.info("Resend verification email requested for: {}", email);

        // Le super admin n'a pas besoin de vérification d'email
        if (superAdminEmail.equals(email)) {
            log.warn("Email verification requested for super admin");
            throw new InvalidCredentialsException("Super admin email does not require verification");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getIsEmailVerified()) {
            log.warn("Email already verified for user: {}", email);
            throw new InvalidCredentialsException("Email already verified");
        }

        // Générer un nouveau token de vérification
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);

        // Renvoyer l'email de vérification
        try {
            emailService.sendWelcomeEmail(user);
            log.info("Verification email resent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to resend verification email");
        }
    }
}