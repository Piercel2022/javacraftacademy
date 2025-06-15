package com.javacraftacademy.userservice.controller;

// Spring Framework imports
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Java Validation imports
import jakarta.validation.Valid;

// Java Standard imports
import java.util.List;

// Services imports
import com.javacraftacademy.userservice.service.ReportService;
import com.javacraftacademy.userservice.service.ModerationService;

// DTOs imports
import com.javacraftacademy.userservice.model.dto.request.*;
import com.javacraftacademy.userservice.model.dto.response.*;

// Exceptions imports
import com.javacraftacademy.userservice.exception.ReportNotFoundException;
import com.javacraftacademy.userservice.exception.UnauthorizedReportAccessException;
import com.javacraftacademy.userservice.exception.DuplicateReportException;
import com.javacraftacademy.userservice.exception.InvalidReportStatusException;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('USER')")
@Validated
public class ReportController {
    private final ReportService reportService;
    private final ModerationService moderationService;

    public ReportController(ReportService reportService, ModerationService moderationService) {
        this.reportService = reportService;
        this.moderationService = moderationService;
    }

    // ===== User Reports =====
    @PostMapping("/user")
    public ResponseEntity<UserReportDto> reportUser(
            @Valid @RequestBody CreateUserReportDto reportDto,
            Authentication authentication) {
        String reporterUsername = authentication.getName();
        UserReportDto report = reportService.reportUser(reporterUsername, reportDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<UserReportDto>> getUserReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserReportDto> reports = reportService.getUserReports(username, status, pageable);
        return ResponseEntity.ok(reports);
    }

    // ===== Content Reports =====
    @PostMapping("/content")
    public ResponseEntity<ContentReportDto> reportContent(
            @Valid @RequestBody CreateContentReportDto reportDto,
            Authentication authentication) {
        String reporterUsername = authentication.getName();
        ContentReportDto report = reportService.reportContent(reporterUsername, reportDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/content")
    public ResponseEntity<Page<ContentReportDto>> getContentReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContentReportDto> reports = reportService.getContentReports(username, status, pageable);
        return ResponseEntity.ok(reports);
    }

    // ===== Report Details =====
    @GetMapping("/user/{reportId}")
    public ResponseEntity<UserReportDto> getUserReportById(
            @PathVariable Long reportId,
            Authentication authentication) {
        String username = authentication.getName();
        UserReportDto report = reportService.getUserReportById(reportId, username);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/content/{reportId}")
    public ResponseEntity<ContentReportDto> getContentReportById(
            @PathVariable Long reportId,
            Authentication authentication) {
        String username = authentication.getName();
        ContentReportDto report = reportService.getContentReportById(reportId, username);
        return ResponseEntity.ok(report);
    }

    // ===== Report Management =====
    @PutMapping("/user/{reportId}")
    public ResponseEntity<UserReportDto> updateUserReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportDto updateDto,
            Authentication authentication) {
        String username = authentication.getName();
        UserReportDto report = reportService.updateUserReport(reportId, updateDto, username);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/content/{reportId}")
    public ResponseEntity<ContentReportDto> updateContentReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportDto updateDto,
            Authentication authentication) {
        String username = authentication.getName();
        ContentReportDto report = reportService.updateContentReport(reportId, updateDto, username);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/user/{reportId}")
    public ResponseEntity<Void> deleteUserReport(
            @PathVariable Long reportId,
            Authentication authentication) {
        String username = authentication.getName();
        reportService.deleteUserReport(reportId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/content/{reportId}")
    public ResponseEntity<Void> deleteContentReport(
            @PathVariable Long reportId,
            Authentication authentication) {
        String username = authentication.getName();
        reportService.deleteContentReport(reportId, username);
        return ResponseEntity.noContent().build();
    }

    // ===== Report Statistics =====
    @GetMapping("/stats")
    public ResponseEntity<ReportStatsDto> getReportStats(
            Authentication authentication) {
        String username = authentication.getName();
        ReportStatsDto stats = reportService.getReportStats(username);
        return ResponseEntity.ok(stats);
    }

    // ===== Moderation Actions =====
    @PostMapping("/user/{reportId}/moderate")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ModerationActionDto> moderateUserReport(
            @PathVariable Long reportId,
            @Valid @RequestBody CreateModerationActionDto actionDto,
            Authentication authentication) {
        String moderatorUsername = authentication.getName();
        ModerationActionDto action = moderationService.moderateUserReport(reportId, actionDto, moderatorUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }

    @PostMapping("/content/{reportId}/moderate")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ModerationActionDto> moderateContentReport(
            @PathVariable Long reportId,
            @Valid @RequestBody CreateModerationActionDto actionDto,
            Authentication authentication) {
        String moderatorUsername = authentication.getName();
        ModerationActionDto action = moderationService.moderateContentReport(reportId, actionDto, moderatorUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }

    // ===== Admin Endpoints =====
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportSummaryDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy != null ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<ReportSummaryDto> reports = reportService.getAllReports(type, status, pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminReportStatsDto> getAdminReportStats() {
        AdminReportStatsDto stats = reportService.getAdminReportStats();
        return ResponseEntity.ok(stats);
    }

    // ===== Report Categories =====
    @GetMapping("/categories")
    public ResponseEntity<List<ReportCategoryDto>> getReportCategories() {
        List<ReportCategoryDto> categories = reportService.getReportCategories();
        return ResponseEntity.ok(categories);
    }

    // ===== Bulk Operations =====
    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<BulkActionResultDto> performBulkAction(
            @Valid @RequestBody BulkReportActionDto bulkActionDto,
            Authentication authentication) {
        String moderatorUsername = authentication.getName();
        BulkActionResultDto result = reportService.performBulkAction(bulkActionDto, moderatorUsername);
        return ResponseEntity.ok(result);
    }

    // ===== Exception Handlers =====
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(ReportNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("REPORT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedReportAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedReportAccessException ex) {
        ErrorResponse error = new ErrorResponse("UNAUTHORIZED_ACCESS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReport(DuplicateReportException ex) {
        ErrorResponse error = new ErrorResponse("DUPLICATE_REPORT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidReportStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReportStatus(InvalidReportStatusException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_REPORT_STATUS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}