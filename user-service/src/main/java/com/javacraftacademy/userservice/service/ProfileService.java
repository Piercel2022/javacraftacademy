package com.javacraftacademy.userservice.service;

import com.javacraftacademy.userservice.exception.UserNotFoundException;
import com.javacraftacademy.userservice.model.dto.request.ProfileUpdateRequest;
import com.javacraftacademy.userservice.model.dto.response.ProfileResponse;
import com.javacraftacademy.userservice.model.entity.Profile;
import com.javacraftacademy.userservice.model.entity.User;
import com.javacraftacademy.userservice.repository.ProfileRepository;
import com.javacraftacademy.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    
    private static final String UPLOAD_DIR = "uploads/profiles/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(createDefaultProfile(user));
        
        return mapToProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> getAllProfiles() {
        log.info("Fetching all profiles");
        
        List<Profile> profiles = profileRepository.findAll();
        
        return profiles.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> getPublicProfiles() {
        log.info("Fetching public profiles");
        
        List<Profile> profiles = profileRepository.findByIsPublicTrue();
        
        return profiles.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> searchProfiles(String keyword) {
        log.info("Searching profiles with keyword: {}", keyword);
        
        List<Profile> profiles = profileRepository.findByBioContainingIgnoreCaseOrSkillsContainingIgnoreCase(
                keyword, keyword);
        
        return profiles.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(createDefaultProfile(user));

        // Mettre à jour les champs du profil
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }
        if (request.getLinkedInUrl() != null) {
            profile.setLinkedInUrl(request.getLinkedInUrl());
        }
        if (request.getGitHubUrl() != null) {
            profile.setGitHubUrl(request.getGitHubUrl());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile updated successfully for user ID: {}", userId);
        
        return mapToProfileResponse(savedProfile);
    }

    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        log.info("Uploading profile image for user ID: {}", userId);
        
        // Valider le fichier
        validateImageFile(file);
        
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "_" + userId + fileExtension;
        Path filePath = uploadPath.resolve(newFilename);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Mettre à jour le profil avec l'URL de l'image
        Profile profile = profileRepository.findByUserId(userId)
                .orElse(createDefaultProfile(user));
        
        // Supprimer l'ancienne image si elle existe
        deleteOldProfileImage(profile.getProfileImageUrl());
        
        String imageUrl = "/uploads/profiles/" + newFilename;
        profile.setProfileImageUrl(imageUrl);
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);

        log.info("Profile image uploaded successfully for user ID: {}", userId);
        return imageUrl;
    }

    public void deleteProfileImage(Long userId) {
        log.info("Deleting profile image for user ID: {}", userId);
        
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user ID: " + userId));

        if (profile.getProfileImageUrl() != null) {
            deleteOldProfileImage(profile.getProfileImageUrl());
            profile.setProfileImageUrl(null);
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
            log.info("Profile image deleted successfully for user ID: {}", userId);
        }
    }

    private Profile createDefaultProfile(User user) {
        log.info("Creating default profile for user ID: {}", user.getId());
        
        Profile profile = Profile.builder()
                .user(user)
                .bio("")
                .location("")
                .website("")
                .skills("")
                .isPublic(true)
                .linkedInUrl("")
                .gitHubUrl("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return profileRepository.save(profile);
    }

    private ProfileResponse mapToProfileResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .userName(profile.getUser().getUsername())
                .userEmail(profile.getUser().getEmail())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .skills(profile.getSkills())
                .profileImageUrl(profile.getProfileImageUrl())
                .isPublic(profile.getIsPublic())
                .linkedInUrl(profile.getLinkedInUrl())
                .gitHubUrl(profile.getGitHubUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private void deleteOldProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Extraire le nom du fichier depuis l'URL
                String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                Path filePath = Paths.get(UPLOAD_DIR + filename);
                
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Old profile image deleted: {}", filename);
                }
            } catch (IOException e) {
                log.warn("Failed to delete old profile image: {}", imageUrl, e);
            }
        }
    }
}


