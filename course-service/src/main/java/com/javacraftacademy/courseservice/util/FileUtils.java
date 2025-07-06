package com.javacraftacademy.courseservice.util;

import com.javacraftacademy.courseservice.exception.InvalidCourseDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilitaire pour la gestion des fichiers dans le service de cours.
 * 
 * Cette classe fournit des méthodes pour :
 * - La validation et le traitement des fichiers uploadés
 * - La génération de noms de fichiers uniques et sécurisés
 * - La gestion des types de fichiers autorisés
 * - Le calcul de hash pour l'intégrité des fichiers
 * - La création et gestion des répertoires de stockage
 * - La conversion et redimensionnement d'images
 * 
 * Relations avec l'application :
 * - Utilisé par FileStorageService pour les opérations de stockage
 * - Intégré dans CourseController et LessonController pour l'upload de contenu
 * - Référencé par CourseContent et Lesson pour la gestion des médias
 * - Utilisé dans la validation des données de cours
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    // Configuration des chemins de stockage
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    @Value("${app.file.allowed-extensions:jpg,jpeg,png,gif,pdf,mp4,avi,mov,doc,docx,ppt,pptx}")
    private String allowedExtensions;

    // Constantes pour les types de fichiers
    public static final String IMAGE_CATEGORY = "image";
    public static final String VIDEO_CATEGORY = "video";
    public static final String DOCUMENT_CATEGORY = "document";
    public static final String AUDIO_CATEGORY = "audio";

    // Extensions autorisées par catégorie
    private static final Map<String, Set<String>> CATEGORY_EXTENSIONS = Map.of(
        IMAGE_CATEGORY, Set.of("jpg", "jpeg", "png", "gif", "webp", "svg"),
        VIDEO_CATEGORY, Set.of("mp4", "avi", "mov", "wmv", "flv", "webm"),
        DOCUMENT_CATEGORY, Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt"),
        AUDIO_CATEGORY, Set.of("mp3", "wav", "ogg", "m4a", "aac")
    );

    // Patterns pour la validation
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern DANGEROUS_EXTENSIONS = Pattern.compile("\\.(exe|bat|cmd|scr|pif|jar|com)$", Pattern.CASE_INSENSITIVE);

    /**
     * Valide un fichier uploadé selon les critères de sécurité et de taille.
     * 
     * @param file Le fichier à valider
     * @throws InvalidCourseDataException si le fichier n'est pas valide
     */
    public void validateFile(MultipartFile file) {
        logger.debug("Validation du fichier: {}", file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            throw new InvalidCourseDataException("Le fichier ne peut pas être vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidCourseDataException(
                String.format("La taille du fichier (%d bytes) dépasse la limite autorisée (%d bytes)", 
                    file.getSize(), maxFileSize)
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new InvalidCourseDataException("Nom de fichier invalide");
        }

        // Vérification de l'extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isExtensionAllowed(extension)) {
            throw new InvalidCourseDataException(
                String.format("Extension de fichier non autorisée: %s", extension)
            );
        }

        // Vérification des extensions dangereuses
        if (DANGEROUS_EXTENSIONS.matcher(originalFilename).find()) {
            throw new InvalidCourseDataException("Type de fichier potentiellement dangereux détecté");
        }

        logger.debug("Fichier validé avec succès: {}", originalFilename);
    }

    /**
     * Génère un nom de fichier unique et sécurisé.
     * 
     * @param originalFilename Le nom original du fichier
     * @param prefix Préfixe optionnel pour organiser les fichiers
     * @return Un nom de fichier unique
     */
    public String generateUniqueFilename(String originalFilename, String prefix) {
        String extension = getFileExtension(originalFilename);
        String baseName = getBaseName(originalFilename);
        
        // Nettoyage du nom de base
        String cleanBaseName = sanitizeFilename(baseName);
        
        // Génération d'un identifiant unique
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        
        StringBuilder filename = new StringBuilder();
        if (StringUtils.hasText(prefix)) {
            filename.append(sanitizeFilename(prefix)).append("_");
        }
        filename.append(cleanBaseName)
                .append("_")
                .append(timestamp)
                .append("_")
                .append(randomId);
        
        if (StringUtils.hasText(extension)) {
            filename.append(".").append(extension.toLowerCase());
        }
        
        return filename.toString();
    }

    /**
     * Génère un nom de fichier unique sans préfixe.
     * 
     * @param originalFilename Le nom original du fichier
     * @return Un nom de fichier unique
     */
    public String generateUniqueFilename(String originalFilename) {
        return generateUniqueFilename(originalFilename, null);
    }

    /**
     * Extrait l'extension d'un nom de fichier.
     * 
     * @param filename Le nom du fichier
     * @return L'extension du fichier (sans le point)
     */
    public String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        
        return "";
    }

    /**
     * Extrait le nom de base d'un fichier (sans extension).
     * 
     * @param filename Le nom du fichier
     * @return Le nom de base du fichier
     */
    public String getBaseName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        
        return filename;
    }

    /**
     * Nettoie un nom de fichier en supprimant les caractères dangereux.
     * 
     * @param filename Le nom de fichier à nettoyer
     * @return Le nom de fichier nettoyé
     */
    public String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unnamed";
        }
        
        // Suppression des caractères spéciaux et espaces
        String sanitized = filename.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
        
        // Limitation de la longueur
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        return StringUtils.hasText(sanitized) ? sanitized : "unnamed";
    }

    /**
     * Détermine la catégorie d'un fichier basée sur son extension.
     * 
     * @param filename Le nom du fichier
     * @return La catégorie du fichier
     */
    public String getFileCategory(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        for (Map.Entry<String, Set<String>> entry : CATEGORY_EXTENSIONS.entrySet()) {
            if (entry.getValue().contains(extension)) {
                return entry.getKey();
            }
        }
        
        return "other";
    }

    /**
     * Vérifie si une extension de fichier est autorisée.
     * 
     * @param extension L'extension à vérifier
     * @return true si l'extension est autorisée, false sinon
     */
    public boolean isExtensionAllowed(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        
        String lowerExtension = extension.toLowerCase();
        Set<String> allowedSet = Set.of(allowedExtensions.toLowerCase().split(","));
        
        return allowedSet.contains(lowerExtension);
    }

    /**
     * Calcule le hash SHA-256 d'un fichier pour vérifier son intégrité.
     * 
     * @param file Le fichier dont calculer le hash
     * @return Le hash SHA-256 en format hexadécimal
     * @throws InvalidCourseDataException en cas d'erreur de calcul
     */
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Erreur lors du calcul du hash du fichier", e);
            throw new InvalidCourseDataException("Impossible de calculer l'empreinte du fichier");
        }
    }

    /**
     * Calcule le hash SHA-256 d'un fichier existant.
     * 
     * @param filePath Le chemin vers le fichier
     * @return Le hash SHA-256 en format hexadécimal
     * @throws InvalidCourseDataException en cas d'erreur de calcul
     */
    public String calculateFileHash(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(filePath));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Erreur lors du calcul du hash du fichier: {}", filePath, e);
            throw new InvalidCourseDataException("Impossible de calculer l'empreinte du fichier");
        }
    }

    /**
     * Crée les répertoires nécessaires pour le stockage des fichiers.
     * 
     * @param subPath Sous-répertoire à créer (par exemple "courses/123/lessons")
     * @return Le chemin complet créé
     * @throws InvalidCourseDataException si la création échoue
     */
    public Path createDirectories(String subPath) {
        try {
            Path fullPath = Paths.get(uploadDir, subPath);
            Files.createDirectories(fullPath);
            logger.debug("Répertoires créés: {}", fullPath.toAbsolutePath());
            return fullPath;
        } catch (IOException e) {
            logger.error("Impossible de créer les répertoires: {}", subPath, e);
            throw new InvalidCourseDataException("Erreur lors de la création des répertoires de stockage");
        }
    }

    /**
     * Construit le chemin de stockage pour un fichier de cours.
     * 
     * @param courseId L'identifiant du cours
     * @param lessonId L'identifiant de la leçon (optionnel)
     * @param fileType Le type de fichier (image, video, document)
     * @return Le chemin de stockage
     */
    public String buildStoragePath(Long courseId, Long lessonId, String fileType) {
        StringBuilder path = new StringBuilder("courses")
                .append(File.separator)
                .append(courseId);
        
        if (lessonId != null) {
            path.append(File.separator)
                .append("lessons")
                .append(File.separator)
                .append(lessonId);
        }
        
        if (StringUtils.hasText(fileType)) {
            path.append(File.separator).append(fileType);
        }
        
        return path.toString();
    }

    /**
     * Formate la taille d'un fichier en format lisible.
     * 
     * @param bytes La taille en bytes
     * @return La taille formatée (ex: "1.5 MB")
     */
    public String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        double size = bytes / Math.pow(1024, unitIndex);
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Vérifie si un fichier existe sur le système de fichiers.
     * 
     * @param filePath Le chemin vers le fichier
     * @return true si le fichier existe, false sinon
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(uploadDir, filePath));
    }

    /**
     * Supprime un fichier du système de fichiers.
     * 
     * @param filePath Le chemin vers le fichier à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(uploadDir, filePath);
            boolean deleted = Files.deleteIfExists(fullPath);
            if (deleted) {
                logger.debug("Fichier supprimé: {}", fullPath.toAbsolutePath());
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier: {}", filePath, e);
            return false;
        }
    }

    /**
     * Obtient les informations détaillées d'un fichier.
     * 
     * @param file Le fichier à analyser
     * @return Une map contenant les informations du fichier
     */
    public Map<String, Object> getFileInfo(MultipartFile file) {
        Map<String, Object> info = new HashMap<>();
        
        info.put("originalName", file.getOriginalFilename());
        info.put("size", file.getSize());
        info.put("formattedSize", formatFileSize(file.getSize()));
        info.put("contentType", file.getContentType());
        info.put("extension", getFileExtension(file.getOriginalFilename()));
        info.put("category", getFileCategory(file.getOriginalFilename()));
        info.put("hash", calculateFileHash(file));
        info.put("uploadTimestamp", LocalDateTime.now());
        
        return info;
    }

    /**
     * Valide un chemin de fichier pour éviter les attaques de traversée de répertoire.
     * 
     * @param filePath Le chemin de fichier à valider
     * @return true si le chemin est sécurisé, false sinon
     */
    public boolean isPathSafe(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        
        // Vérification des tentatives de traversée de répertoire
        return !filePath.contains("..") && 
               !filePath.startsWith("/") && 
               !filePath.contains("\\") &&
               SAFE_FILENAME_PATTERN.matcher(Paths.get(filePath).getFileName().toString()).matches();
    }

    /**
     * Nettoie les fichiers temporaires anciens.
     * Cette méthode peut être appelée périodiquement pour maintenir l'espace de stockage.
     * 
     * @param olderThanDays Supprimer les fichiers plus anciens que ce nombre de jours
     * @return Le nombre de fichiers supprimés
     */
    public int cleanupTempFiles(int olderThanDays) {
        logger.info("Nettoyage des fichiers temporaires de plus de {} jours", olderThanDays);
        
        Path tempDir = Paths.get(uploadDir, "temp");
        if (!Files.exists(tempDir)) {
            return 0;
        }
        
        int deletedCount = 0;
        try {
            long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24L * 60L * 60L * 1000L);
            
            Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        deletedCount++;
                        logger.debug("Fichier temporaire supprimé: {}", path);
                    } catch (IOException e) {
                        logger.warn("Impossible de supprimer le fichier temporaire: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            logger.error("Erreur lors du nettoyage des fichiers temporaires", e);
        }
        
        logger.info("Nettoyage terminé. {} fichiers supprimés", deletedCount);
        return deletedCount;
    }

    // Getters pour les propriétés de configuration
    public String getUploadDir() {
        return uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }
}