package com.javacraftacademy.userservice.controller;

import com.javacraftacademy.userservice.model.dto.response.UserResponse;
import com.javacraftacademy.userservice.model.dto.response.ApiResponse;
import com.javacraftacademy.userservice.model.entity.User;
import com.javacraftacademy.userservice.service.UserService;
import com.javacraftacademy.userservice.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            UserResponse userResponse = userService.getCurrentUser(userPrincipal.getId());
            ApiResponse<UserResponse> response = new ApiResponse<>(
                true, 
                "User retrieved successfully", 
                userResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            UserResponse userResponse = userService.getUserById(id);
            ApiResponse<UserResponse> response = new ApiResponse<>(
                true, 
                "User retrieved successfully", 
                userResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        try {
            UserResponse userResponse = userService.getUserByEmail(email);
            ApiResponse<UserResponse> response = new ApiResponse<>(
                true, 
                "User retrieved successfully", 
                userResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<UserResponse> users = userService.getAllUsers(pageable);
            
            ApiResponse<Page<UserResponse>> response = new ApiResponse<>(
                true, 
                "Users retrieved successfully", 
                users
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<UserResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<UserResponse> users = userService.searchUsers(query, limit);
            ApiResponse<List<UserResponse>> response = new ApiResponse<>(
                true, 
                "Search completed successfully", 
                users
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<UserResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "User activated successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "User deactivated successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUserRole(
            @PathVariable Long id, 
            @RequestParam String roleName) {
        try {
            userService.updateUserRole(id, roleName);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "User role updated successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "User deleted successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getUserStats() {
        try {
            Object stats = userService.getUserStats();
            ApiResponse<Object> response = new ApiResponse<>(
                true, 
                "User statistics retrieved successfully", 
                stats
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Object> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        try {
            userService.changePassword(userPrincipal.getId(), currentPassword, newPassword);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Password changed successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/me/email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> changeEmail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String newEmail,
            @RequestParam String password) {
        try {
            userService.changeEmail(userPrincipal.getId(), newEmail, password);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Email change initiated. Please check your new email for verification.", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

