package com.javacraftacademy.userservice.controller;

import com.javacraftacademy.userservice.model.dto.request.ProfileUpdateRequest;
import com.javacraftacademy.userservice.model.dto.response.ProfileResponse;
import com.javacraftacademy.userservice.model.dto.response.ApiResponse;
import com.javacraftacademy.userservice.service.ProfileService;
import com.javacraftacademy.userservice.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProfileResponse profileResponse = profileService.getProfileByUserId(userPrincipal.getId());
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                true, 
                "Profile retrieved successfully", 
                profileResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileById(@PathVariable Long id) {
        try {
            ProfileResponse profileResponse = profileService.getProfileById(id);
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                true, 
                "Profile retrieved successfully", 
                profileResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(@PathVariable Long userId) {
        try {
            ProfileResponse profileResponse = profileService.getProfileByUserId(userId);
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                true, 
                "Profile retrieved successfully", 
                profileResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        try {
            ProfileResponse profileResponse = profileService.updateProfile(userPrincipal.getId(), profileUpdateRequest);
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                true, 
                "Profile updated successfully", 
                profileResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        try {
            ProfileResponse profileResponse = profileService.updateProfile(userId, profileUpdateRequest);
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                true, 
                "Profile updated successfully", 
                profileResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ProfileResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) {
        try {
            String avatarUrl = profileService.uploadAvatar(userPrincipal.getId(), file);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Avatar uploaded successfully", 
                avatarUrl
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

    @DeleteMapping("/me/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAvatar(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            profileService.deleteAvatar(userPrincipal.getId());
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Avatar deleted successfully", 
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

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> searchProfiles(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProfileResponse> profiles = profileService.searchProfiles(query, limit);
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                true, 
                "Search completed successfully", 
                profiles
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/skills/{skill}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getProfilesBySkill(
            @PathVariable String skill,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProfileResponse> profiles = profileService.getProfilesBySkill(skill, limit);
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                true, 
                "Profiles retrieved successfully", 
                profiles
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/location/{location}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getProfilesByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProfileResponse> profiles = profileService.getProfilesByLocation(location, limit);
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                true, 
                "Profiles retrieved successfully", 
                profiles
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/me/privacy")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updatePrivacySettings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam boolean isPublic) {
        try {
            profileService.updatePrivacySettings(userPrincipal.getId(), isPublic);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Privacy settings updated successfully", 
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

    @PutMapping("/me/notification-preferences")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam boolean emailNotifications,
            @RequestParam boolean smsNotifications,
            @RequestParam boolean pushNotifications) {
        try {
            profileService.updateNotificationPreferences(
                userPrincipal.getId(), 
                emailNotifications, 
                smsNotifications, 
                pushNotifications
            );
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Notification preferences updated successfully", 
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

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCurrentUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            profileService.deleteProfile(userPrincipal.getId());
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Profile deleted successfully", 
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

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@PathVariable Long userId) {
        try {
            profileService.deleteProfile(userId);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Profile deleted successfully", 
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<ProfileResponse> profiles = profileService.getAllProfiles(page, size);
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                true, 
                "Profiles retrieved successfully", 
                profiles
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ProfileResponse>> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getProfileStatistics() {
        try {
            Object statistics = profileService.getProfileStatistics();
            ApiResponse<Object> response = new ApiResponse<>(
                true, 
                "Statistics retrieved successfully", 
                statistics
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
}


