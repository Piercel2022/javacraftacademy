package com.javacraftacademy.userservice.security;

import com.javacraftacademy.userservice.model.entity.User;
import com.javacraftacademy.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service personnalisé pour charger les détails utilisateur
 * Implémente UserDetailsService pour l'authentification Spring Security
 * Implémente UserDetailsService de Spring Security
 * Charge les détails utilisateur depuis la base de données
 * Fournit des méthodes pour charger par email ou par ID
 * Gère les utilisateurs supprimés (soft delete)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return UserPrincipal.create(user);
    }

    /**
     * Charge un utilisateur par son ID
     * Utilisé pour la validation des tokens JWT
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}