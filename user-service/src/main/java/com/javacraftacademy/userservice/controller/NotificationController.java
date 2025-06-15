
package com.javacraftacademy.userservice.controller;

import com.javacraftacademy.userservice.model.dto.response.NotificationResponse;
import com.javacraftacademy.userservice.model.dto.request.NotificationPreferencesRequest;
import com.javacraftacademy.userservice.model.dto.response.NotificationPreferencesResponse;
import com.javacraftacademy.userservice.model.dto.response.ApiResponse;
import com.javacraftacademy.userservice.service.NotificationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * REST Controller for managing user notifications
 * Handles notification retrieval, marking as read, deletion, and preferences management
 */
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('USER')")
@Validated
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Get paginated notifications for the authenticated user
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param unreadOnly Filter for unread notifications only (default: false)
     * @param authentication Current user authentication
     * @return Page of notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Authentication authentication) {
        
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<NotificationResponse> notifications = notificationService.getNotifications(
            username, unreadOnly, pageable);
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get count of unread notifications for the authenticated user
     * @param authentication Current user authentication
     * @return Map containing unread count
     */
    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
    
    /**
     * Mark a specific notification as read
     * @param notificationId ID of the notification to mark as read
     * @param authentication Current user authentication
     * @return Empty response
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        String username = authentication.getName();
        notificationService.markAsRead(notificationId, username);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Notification marked as read successfully")
                .build()
        );
    }
    
    /**
     * Mark all notifications as read for the authenticated user
     * @param authentication Current user authentication
     * @return Empty response
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAllAsRead(username);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read successfully")
                .build()
        );
    }
    
    /**
     * Delete a specific notification
     * @param notificationId ID of the notification to delete
     * @param authentication Current user authentication
     * @return Empty response
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        String username = authentication.getName();
        notificationService.deleteNotification(notificationId, username);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Notification deleted successfully")
                .build()
        );
    }
    
    /**
     * Get notification preferences for the authenticated user
     * @param authentication Current user authentication
     * @return User's notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            Authentication authentication) {
        
        String username = authentication.getName();
        NotificationPreferencesResponse preferences = notificationService.getPreferences(username);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Update notification preferences for the authenticated user
     * @param preferencesRequest New notification preferences
     * @param authentication Current user authentication
     * @return Updated notification preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @Valid @RequestBody NotificationPreferencesRequest preferencesRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        NotificationPreferencesResponse updated = notificationService.updatePreferences(
            username, preferencesRequest);
        return ResponseEntity.ok(updated);
    }
}