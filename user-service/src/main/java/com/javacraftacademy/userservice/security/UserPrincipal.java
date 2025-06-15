package com.javacraftacademy.userservice.security;

import com.javacraftacademy.userservice.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Principal utilisateur personnalisé pour Spring Security
 * Encapsule les informations utilisateur pour l'authentification
 * Implémente UserDetails pour représenter l'utilisateur authentifié
 * Encapsule toutes les informations utilisateur nécessaires à la sécurité
 * Gère les rôles et permissions
 * Fournit des méthodes utilitaires pour vérifier les autorisations
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean enabled;
    private boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Crée un UserPrincipal à partir d'une entité User
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isEmailVerified(),
                user.getLastLoginAt(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled && emailVerified;
    }

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName.toUpperCase()));
    }

    /**
     * Vérifie si l'utilisateur a une autorité spécifique
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
}