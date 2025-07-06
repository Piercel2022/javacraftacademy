package com.javacraftacademy.courseservice.dto.mapper;

import com.javacraftacademy.courseservice.dto.request.EnrollmentRequest;
import com.javacraftacademy.courseservice.dto.response.EnrollmentResponse;
import com.javacraftacademy.courseservice.model.entity.Enrollment;
import com.javacraftacademy.courseservice.model.entity.Course;
import com.javacraftacademy.courseservice.model.enums.EnrollmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component responsible for converting between Enrollment entities and DTOs.
 * 
 * <p>This component provides bidirectional mapping functionality between:
 * <ul>
 *   <li>{@link Enrollment} entities and {@link EnrollmentResponse} DTOs</li>
 *   <li>{@link EnrollmentRequest} DTOs and {@link Enrollment} entities</li>
 * </ul>
 * 
 * <p><strong>Architecture Role:</strong>
 * The EnrollmentMapper acts as a data transformation layer in the course service architecture,
 * sitting between the controller and service layers. It ensures clean separation between
 * internal entity representations and external API contracts.
 * 
 * <p><strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Transform enrollment entities to response DTOs for API consumers</li>
 *   <li>Convert enrollment requests to entities for business logic processing</li>
 *   <li>Handle enrollment status and progress calculations</li>
 *   <li>Manage enrollment metadata like enrollment date and completion status</li>
 *   <li>Support bulk mapping operations for efficient data processing</li>
 * </ul>
 * 
 * <p><strong>Component Relationships:</strong>
 * <ul>
 *   <li><strong>Controllers:</strong> Used by {@code EnrollmentController} to transform data</li>
 *   <li><strong>Services:</strong> Integrated with {@code EnrollmentService} for data conversion</li>
 *   <li><strong>Entities:</strong> Maps {@code Enrollment}, {@code Course} entities</li>
 *   <li><strong>DTOs:</strong> Works with {@code EnrollmentRequest/Response} objects</li>
 *   <li><strong>Other Mappers:</strong> Collaborates with {@code CourseMapper} for nested data</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong>
 * <pre>
 * // Convert entity to response DTO
 * EnrollmentResponse response = enrollmentMapper.toResponse(enrollment);
 * 
 * // Convert request DTO to entity
 * Enrollment enrollment = enrollmentMapper.toEntity(request, course);
 * 
 * // Bulk conversion
 * List&lt;EnrollmentResponse&gt; responses = enrollmentMapper.toResponseList(enrollments);
 * </pre>
 * 
 * @author JavaCraft Academy Development Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Enrollment
 * @see EnrollmentRequest
 * @see EnrollmentResponse
 * @see com.javacraftacademy.courseservice.controller.EnrollmentController
 * @see com.javacraftacademy.courseservice.service.EnrollmentService
 */
@Component
public class EnrollmentMapper {

    private final CourseMapper courseMapper;

    /**
     * Constructor for dependency injection.
     * 
     * @param courseMapper the course mapper for handling nested course data
     */
    public EnrollmentMapper(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    /**
     * Converts an Enrollment entity to an EnrollmentResponse DTO.
     * 
     * <p>This method transforms the internal enrollment representation into a format
     * suitable for API responses. It includes enrollment metadata, course information,
     * progress tracking, and status details.
     * 
     * <p><strong>Transformation Details:</strong>
     * <ul>
     *   <li>Maps all enrollment identifiers and metadata</li>
     *   <li>Includes course information through CourseMapper</li>
     *   <li>Calculates progress percentage if applicable</li>
     *   <li>Formats dates for API consumption</li>
     *   <li>Includes enrollment status and completion information</li>
     * </ul>
     * 
     * @param enrollment the enrollment entity to convert
     * @return the corresponding enrollment response DTO
     * @throws IllegalArgumentException if enrollment is null
     * 
     * @see EnrollmentResponse
     * @see toResponseList(List)
     */
    public EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null");
        }

        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setUserId(enrollment.getUserId());
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setStatus(enrollment.getStatus());
        response.setProgress(enrollment.getProgress());
        response.setCompletedAt(enrollment.getCompletedAt());
        response.setLastAccessedAt(enrollment.getLastAccessedAt());
        response.setCertificateUrl(enrollment.getCertificateUrl());
        response.setNotes(enrollment.getNotes());

        // Map course information if available
        if (enrollment.getCourse() != null) {
            response.setCourse(courseMapper.toResponse(enrollment.getCourse()));
        }

        // Calculate additional metrics
        response.setIsCompleted(enrollment.getStatus() == EnrollmentStatus.COMPLETED);
        response.setProgressPercentage(calculateProgressPercentage(enrollment));
        response.setDaysEnrolled(calculateDaysEnrolled(enrollment));

        return response;
    }

    /**
     * Converts an EnrollmentRequest DTO to an Enrollment entity.
     * 
     * <p>This method creates a new enrollment entity from request data, setting up
     * the initial state and establishing relationships with the associated course.
     * 
     * <p><strong>Entity Creation Process:</strong>
     * <ul>
     *   <li>Creates new enrollment with default status (ACTIVE)</li>
     *   <li>Sets enrollment date to current timestamp</li>
     *   <li>Establishes relationship with the provided course</li>
     *   <li>Initializes progress tracking fields</li>
     *   <li>Sets user identifier from request</li>
     * </ul>
     * 
     * @param request the enrollment request containing user and enrollment data
     * @param course the course entity to enroll in
     * @return a new enrollment entity ready for persistence
     * @throws IllegalArgumentException if request or course is null
     * 
     * @see EnrollmentRequest
     * @see Course
     * @see #updateFromRequest(Enrollment, EnrollmentRequest)
     */
    public Enrollment toEntity(EnrollmentRequest request, Course course) {
        if (request == null) {
            throw new IllegalArgumentException("EnrollmentRequest cannot be null");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(request.getUserId());
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(0.0);
        enrollment.setNotes(request.getNotes());
        enrollment.setLastAccessedAt(LocalDateTime.now());

        return enrollment;
    }

    /**
     * Updates an existing Enrollment entity from an EnrollmentRequest DTO.
     * 
     * <p>This method allows partial updates of enrollment data while preserving
     * system-managed fields like enrollment date and course relationships.
     * 
     * <p><strong>Update Strategy:</strong>
     * <ul>
     *   <li>Updates only user-modifiable fields</li>
     *   <li>Preserves system timestamps and identifiers</li>
     *   <li>Maintains course relationship integrity</li>
     *   <li>Updates last accessed timestamp</li>
     * </ul>
     * 
     * @param enrollment the existing enrollment entity to update
     * @param request the request containing updated data
     * @throws IllegalArgumentException if enrollment or request is null
     * 
     * @see #toEntity(EnrollmentRequest, Course)
     */
    public void updateFromRequest(Enrollment enrollment, EnrollmentRequest request) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("EnrollmentRequest cannot be null");
        }

        // Update only modifiable fields
        if (request.getNotes() != null) {
            enrollment.setNotes(request.getNotes());
        }
        
        // Update last accessed timestamp
        enrollment.setLastAccessedAt(LocalDateTime.now());
    }

    /**
     * Converts a list of Enrollment entities to EnrollmentResponse DTOs.
     * 
     * <p>This method provides efficient bulk conversion for scenarios involving
     * multiple enrollments, such as user enrollment history or course enrollment lists.
     * 
     * <p><strong>Performance Considerations:</strong>
     * <ul>
     *   <li>Uses Java 8 Streams for efficient processing</li>
     *   <li>Maintains order of input collection</li>
     *   <li>Filters out null entries automatically</li>
     *   <li>Suitable for large datasets with proper JVM tuning</li>
     * </ul>
     * 
     * @param enrollments the list of enrollment entities to convert
     * @return a list of enrollment response DTOs
     * @throws IllegalArgumentException if enrollments list is null
     * 
     * @see #toResponse(Enrollment)
     * @see toResponseList(List, boolean)
     */
    public List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments) {
        if (enrollments == null) {
            throw new IllegalArgumentException("Enrollments list cannot be null");
        }

        return enrollments.stream()
                .filter(enrollment -> enrollment != null)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of Enrollment entities to EnrollmentResponse DTOs with options.
     * 
     * <p>This overloaded method provides additional control over the conversion process,
     * including the ability to include or exclude course details for performance optimization.
     * 
     * @param enrollments the list of enrollment entities to convert
     * @param includeCourseDetails whether to include full course information
     * @return a list of enrollment response DTOs
     * @throws IllegalArgumentException if enrollments list is null
     * 
     * @see toResponseList(List)
     */
    public List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments, boolean includeCourseDetails) {
        if (enrollments == null) {
            throw new IllegalArgumentException("Enrollments list cannot be null");
        }

        return enrollments.stream()
                .filter(enrollment -> enrollment != null)
                .map(enrollment -> includeCourseDetails ? 
                    toResponse(enrollment) : 
                    toResponseWithoutCourse(enrollment))
                .collect(Collectors.toList());
    }

    /**
     * Creates a simplified enrollment response without course details.
     * 
     * <p>This method is useful for performance optimization when course details
     * are not needed or will be fetched separately.
     * 
     * @param enrollment the enrollment entity to convert
     * @return enrollment response without course details
     */
    private EnrollmentResponse toResponseWithoutCourse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setUserId(enrollment.getUserId());
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setStatus(enrollment.getStatus());
        response.setProgress(enrollment.getProgress());
        response.setCompletedAt(enrollment.getCompletedAt());
        response.setLastAccessedAt(enrollment.getLastAccessedAt());
        response.setCertificateUrl(enrollment.getCertificateUrl());
        response.setNotes(enrollment.getNotes());
        response.setIsCompleted(enrollment.getStatus() == EnrollmentStatus.COMPLETED);
        response.setProgressPercentage(calculateProgressPercentage(enrollment));
        response.setDaysEnrolled(calculateDaysEnrolled(enrollment));
        
        // Set course ID only if course exists
        if (enrollment.getCourse() != null) {
            response.setCourseId(enrollment.getCourse().getId());
        }

        return response;
    }

    /**
     * Calculates the progress percentage for an enrollment.
     * 
     * <p>This method converts the raw progress value (0.0 to 1.0) to a
     * user-friendly percentage (0 to 100).
     * 
     * @param enrollment the enrollment entity
     * @return progress as a percentage (0-100)
     */
    private Integer calculateProgressPercentage(Enrollment enrollment) {
        if (enrollment.getProgress() == null) {
            return 0;
        }
        return (int) Math.round(enrollment.getProgress() * 100);
    }

    /**
     * Calculates the number of days since enrollment.
     * 
     * <p>This method provides a human-readable metric for enrollment duration,
     * useful for analytics and user engagement tracking.
     * 
     * @param enrollment the enrollment entity
     * @return number of days since enrollment
     */
    private Long calculateDaysEnrolled(Enrollment enrollment) {
        if (enrollment.getEnrollmentDate() == null) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            enrollment.getEnrollmentDate().toLocalDate(),
            LocalDateTime.now().toLocalDate()
        );
    }

    /**
     * Creates a summary enrollment response for dashboard views.
     * 
     * <p>This method creates a lightweight version of enrollment data suitable
     * for dashboard displays, listing pages, or mobile applications where
     * bandwidth is a concern.
     * 
     * @param enrollment the enrollment entity
     * @return a summarized enrollment response
     */
    public EnrollmentResponse toSummaryResponse(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null");
        }

        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setUserId(enrollment.getUserId());
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setStatus(enrollment.getStatus());
        response.setProgress(enrollment.getProgress());
        response.setIsCompleted(enrollment.getStatus() == EnrollmentStatus.COMPLETED);
        response.setProgressPercentage(calculateProgressPercentage(enrollment));

        // Include minimal course information
        if (enrollment.getCourse() != null) {
            response.setCourseId(enrollment.getCourse().getId());
            response.setCourseTitle(enrollment.getCourse().getTitle());
        }

        return response;
    }

    /**
     * Validates enrollment data consistency.
     * 
     * <p>This method performs validation checks to ensure data integrity
     * during mapping operations.
     * 
     * @param enrollment the enrollment to validate
     * @return true if enrollment data is consistent
     */
    public boolean validateEnrollmentData(Enrollment enrollment) {
        if (enrollment == null) {
            return false;
        }

        // Check required fields
        if (enrollment.getUserId() == null || enrollment.getCourse() == null) {
            return false;
        }

        // Validate progress range
        if (enrollment.getProgress() != null && 
            (enrollment.getProgress() < 0.0 || enrollment.getProgress() > 1.0)) {
            return false;
        }

        // Validate status consistency
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED && 
            enrollment.getCompletedAt() == null) {
            return false;
        }

        return true;
    }
}
