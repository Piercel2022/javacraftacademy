package com.javacraftacademy.userservice.service;

import com.javacraftacademy.userservice.exception.EmailAlreadyExistsException;
import com.javacraftacademy.userservice.exception.UserNotFoundException;
import com.javacraftacademy.userservice.model.dto.response.UserResponse;
import com.javacraftacademy.userservice.model.entity.Role;
import com.javacraftacademy.userservice.model.entity.User;
import com.javacraftacademy.userservice.repository.RoleRepository;
import com.javacraftacademy.userservice.repository.UserRepository;
import com.javacraftacademy.userservice.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching all users - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(this::mapToUserResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, int page, int size, String sortBy, String sortDir) {
        log.info("Searching users with keyword: {} - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                keyword, page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                keyword, keyword, keyword, keyword, pageable);
        
        return users.map(this::mapToUserResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        log.info("Fetching users by role: {}", roleName);
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        List<User> users = userRepository.findByRolesContaining(role);
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.info("Fetching active users");
        
        List<User> users = userRepository.findByIsActiveTrue();
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getVerifiedUsers() {
        log.info("Fetching verified users");
        
        List<User> users = userRepository.findByIsEmailVerifiedTrue();
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long userId, UserResponse userUpdate) {
        log.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (userUpdate.getEmail() != null && !userUpdate.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(userUpdate.getEmail(), userId)) {
                log.warn("Update failed: Email already exists - {}", userUpdate.getEmail());
                throw new EmailAlreadyExistsException("Email already exists: " + userUpdate.getEmail());
            }
            user.setEmail(userUpdate.getEmail());
            user.setIsEmailVerified(false); // Réinitialiser la vérification si l'email change
        }

        // Vérifier si le nom d'utilisateur est déjà utilisé par un autre utilisateur
        if (userUpdate.getUsername() != null && !userUpdate.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(userUpdate.getUsername(), userId)) {
                log.warn("Update failed: Username already exists - {}", userUpdate.getUsername());
                throw new EmailAlreadyExistsException("Username already exists: " + userUpdate.getUsername());
            }
            user.setUsername(userUpdate.getUsername());
        }

        // Mettre à jour les autres champs
        if (userUpdate.getFirstName() != null) {
            user.setFirstName(userUpdate.getFirstName());
        }
        if (userUpdate.getLastName() != null) {
            user.setLastName(userUpdate.getLastName());
        }

        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());
        
        return mapToUserResponse(updatedUser);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed: Invalid current password for user ID: {}", userId);
            throw new RuntimeException("Current password is incorrect");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    public void activateUser(Long userId) {
        log.info("Activating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("User activated successfully: {}", user.getEmail());
    }

    public void deactivateUser(Long userId) {
        log.info("Deactivating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("User deactivated successfully: {}", user.getEmail());
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    public UserResponse assignRole(Long userId, String roleName) {
        log.info("Assigning role {} to user ID: {}", roleName, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        log.info("Role {} assigned successfully to user: {}", roleName, user.getEmail());
        
        return mapToUserResponse(updatedUser);
    }

    public UserResponse removeRole(Long userId, String roleName) {
        log.info("Removing role {} from user ID: {}", roleName, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Empêcher la suppression du rôle par défaut si c'est le seul rôle
        if (user.getRoles().size() == 1 && user.getRoles().contains(role) 
                && Constants.DEFAULT_ROLE.equals(roleName)) {
            log.warn("Cannot remove default role {} - it's the only role for user: {}", 
                    roleName, user.getEmail());
            throw new RuntimeException("Cannot remove the default role when it's the only role");
        }

        user.getRoles().remove(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        log.info("Role {} removed successfully from user: {}", roleName, user.getEmail());
        
        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public long getVerifiedUserCount() {
        return userRepository.countByIsEmailVerifiedTrue();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}


