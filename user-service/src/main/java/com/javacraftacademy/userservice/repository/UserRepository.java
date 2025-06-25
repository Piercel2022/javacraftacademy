package com.javacraftacademy.userservice.repository;

import com.javacraftacademy.userservice.model.entity.Role;
import com.javacraftacademy.userservice.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Méthodes de base
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    // Méthodes corrigées avec les bons noms de propriétés
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
    
    // CORRIGÉ: isEmailVerified au lieu de emailVerified
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false")
    List<User> findUnverifiedUsers();
    
    // CORRIGÉ: lastLoginAt au lieu de lastLoginDate
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate")
    List<User> findInactiveUsersSince(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // CORRIGÉ: createdAt au lieu de createdDate
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByCreationDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);
    
    // CORRIGÉ: Utiliser les méthodes Spring Data JPA au lieu de @Query
    long countByIsActiveTrue();
    
    // CORRIGÉ: Utiliser les méthodes Spring Data JPA au lieu de @Query
    long countByIsEmailVerifiedTrue();
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm% OR u.username LIKE %:searchTerm%")
    List<User> findUsersBySearchTerm(@Param("searchTerm") String searchTerm);
    
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiry > :currentTime")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token,
                                               @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationTokenExpiry > :currentTime")
    Optional<User> findByValidEmailVerificationToken(@Param("token") String token,
                                                   @Param("currentTime") LocalDateTime currentTime);

    // ============ MÉTHODES NÉCESSAIRES POUR LE SERVICE ============
    
    /**
     * Recherche des utilisateurs par mot-clé dans username, email, firstName ou lastName avec pagination
     * Utilisée dans UserService.searchUsers()
     */
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String email, String firstName, String lastName, Pageable pageable);
    
    /**
     * Trouve les utilisateurs qui ont un rôle spécifique
     * Utilisée dans UserService.getUsersByRole()
     */
    List<User> findByRolesContaining(Role role);
    
    /**
     * Trouve tous les utilisateurs actifs
     * Utilisée dans UserService.getActiveUsers()
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Trouve tous les utilisateurs avec email vérifié
     * Utilisée dans UserService.getVerifiedUsers()
     */
    List<User> findByIsEmailVerifiedTrue();
    
    /**
     * Vérifie si un email existe déjà pour un autre utilisateur (excluant l'ID donné)
     * Utilisée dans UserService.updateUser()
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Vérifie si un nom d'utilisateur existe déjà pour un autre utilisateur (excluant l'ID donné)
     * Utilisée dans UserService.updateUser()
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

   

}