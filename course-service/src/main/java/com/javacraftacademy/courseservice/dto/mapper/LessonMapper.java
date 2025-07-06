package com.javacraftacademy.courseservice.dto.mapper;

import com.javacraftacademy.courseservice.dto.request.CreateLessonRequest;
import com.javacraftacademy.courseservice.dto.request.UpdateLessonRequest;
import com.javacraftacademy.courseservice.dto.response.LessonResponse;
import com.javacraftacademy.courseservice.model.entity.Lesson;
import com.javacraftacademy.courseservice.model.entity.Course;
import com.javacraftacademy.courseservice.model.enums.LessonType;
import com.javacraftacademy.courseservice.util.SlugUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mapper component responsible for converting between Lesson entities and DTOs.
 * 
 * <p>This mapper handles the transformation of lesson data between different layers
 * of the application, managing lesson content, metadata, and relationships with
 * parent courses and course content.</p>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Convert Lesson entities to response DTOs for API responses</li>
 *   <li>Transform request DTOs to Lesson entities for persistence</li>
 *   <li>Handle lesson ordering and sequencing within courses</li>
 *   <li>Manage lesson content metadata and file associations</li>
 *   <li>Generate lesson slugs for navigation and SEO</li>
 * </ul>
 * 
 * <h3>Relationships with Application Components:</h3>
 * <ul>
 *   <li><strong>Controllers:</strong> Used by LessonController and CourseController for data transformation</li>
 *   <li><strong>Services:</strong> Utilized by LessonService for CRUD operations and course management</li>
 *   <li><strong>Entities:</strong> Maps Lesson entities and maintains Course relationships</li>
 *   <li><strong>DTOs:</strong> Transforms between lesson request/response DTOs</li>
 *   <li><strong>FileStorage:</strong> Integrates with FileStorageService for content management</li>
 *   <li><strong>CourseMapper:</strong> Works with CourseMapper for nested course-lesson operations</li>
 * </ul>
 * 
 * <h3>Lesson Content Types:</h3>
 * <ul>
 *   <li><strong>VIDEO:</strong> Video-based lessons with streaming capabilities</li>
 *   <li><strong>TEXT:</strong> Text-based lessons with rich content</li>
 *   <li><strong>QUIZ:</strong> Interactive quizzes and assessments</li>
 *   <li><strong>ASSIGNMENT:</strong> Practical assignments and projects</li>
 *   <li><strong>RESOURCE:</strong> Downloadable resources and materials</li>
 * </ul>
 * 
 * @author JavaCraftAcademy
 * @version 1.0
 * @since 1.0
 */
@Component
public class LessonMapper {

    /**
     * Converts a Lesson entity to a LessonResponse DTO for API responses.
     * 
     * <p>This method creates a comprehensive view of the lesson including
     * content metadata, progress tracking information, and access controls.
     * It excludes sensitive information and prepares data for client consumption.</p>
     * 
     * @param lesson The lesson entity to convert
     * @return LessonResponse DTO containing lesson information
     * @throws IllegalArgumentException if lesson is null
     */
    public LessonResponse toResponse(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null");
        }

        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setTitle(lesson.getTitle());
        response.setSlug(lesson.getSlug());
        response.setDescription(lesson.getDescription());
        response.setType(lesson.getType());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setDurationInMinutes(lesson.getDurationInMinutes());
        response.setVideoUrl(lesson.getVideoUrl());
        response.setContentUrl(lesson.getContentUrl());
        response.setThumbnailUrl(lesson.getThumbnailUrl());
        response.setFree(lesson.isFree());
        response.setPublished(lesson.isPublished());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());

        // Include course information if available
        if (lesson.getCourse() != null) {
            response.setCourseId(lesson.getCourse().getId());
            response.setCourseTitle(lesson.getCourse().getTitle());
        }

        // Calculate content metadata
        response.setHasVideo(lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty());
        response.setHasContent(lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty());
        response.setContentSize(calculateContentSize(lesson));

        return response;
    }

    /**
     * Converts a CreateLessonRequest DTO to a Lesson entity.
     * 
     * <p>This method transforms lesson creation requests into new Lesson entities,
     * setting default values and preparing the entity for persistence. The lesson
     * is created with proper ordering and initial metadata.</p>
     * 
     * @param request The lesson creation request DTO
     * @param course The parent course for this lesson
     * @return New Lesson entity ready for persistence
     * @throws IllegalArgumentException if request or course is null
     */
    public Lesson toEntity(CreateLessonRequest request, Course course) {
        if (request == null) {
            throw new IllegalArgumentException("CreateLessonRequest cannot be null");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setSlug(generateLessonSlug(request.getTitle(), course.getSlug()));
        lesson.setDescription(request.getDescription());
        lesson.setType(request.getType() != null ? request.getType() : LessonType.VIDEO);
        lesson.setDurationInMinutes(request.getDurationInMinutes() != null ? request.getDurationInMinutes() : 0);
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setThumbnailUrl(request.getThumbnailUrl());
        lesson.setFree(request.isFree() != null ? request.isFree() : false);
        lesson.setPublished(false); // New lessons start as unpublished
        lesson.setCourse(course);

        // Set order index (will be set by service based on course's current lesson count)
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);

        // Set timestamps
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        return lesson;
    }

    /**
     * Updates an existing Lesson entity with data from UpdateLessonRequest DTO.
     * 
     * <p>This method selectively updates lesson fields based on the update request,
     * preserving existing data where not specified. It handles content updates,
     * ordering changes, and metadata modifications.</p>
     * 
     * @param lesson The existing lesson entity to update
     * @param request The update request containing new values
     * @throws IllegalArgumentException if lesson or request is null
     */
    public void updateEntity(Lesson lesson, UpdateLessonRequest request) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateLessonRequest cannot be null");
        }

        // Update fields only if provided in request
        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
            // Regenerate slug if title changes
            if (lesson.getCourse() != null) {
                lesson.setSlug(generateLessonSlug(request.getTitle(), lesson.getCourse().getSlug()));
            }
        }

        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription());
        }

        if (request.getType() != null) {
            lesson.setType(request.getType());
        }

        if (request.getDurationInMinutes() != null) {
            lesson.setDurationInMinutes(request.getDurationInMinutes());
        }

        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
        }

        if (request.getContentUrl() != null) {
            lesson.setContentUrl(request.getContentUrl());
        }

        if (request.getThumbnailUrl() != null) {
            lesson.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }

        if (request.getFree() != null) {
            lesson.setFree(request.getFree());
        }

        if (request.getPublished() != null) {
            lesson.setPublished(request.getPublished());
        }

        // Always update the modification timestamp
        lesson.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Converts a list of Lesson entities to a list of LessonResponse DTOs.
     * 
     * <p>Utility method for bulk transformation of lesson entities to response DTOs.
     * The lessons are automatically sorted by their order index to maintain
     * proper lesson sequence.</p>
     * 
     * @param lessons List of lesson entities to convert
     * @return List of LessonResponse DTOs sorted by order index
     */
    public List<LessonResponse> toResponseList(List<Lesson> lessons) {
        if (lessons == null) {
            return List.of();
        }

        return lessons.stream()
                .sorted((l1, l2) -> Integer.compare(l1.getOrderIndex(), l2.getOrderIndex()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a minimal Lesson entity with only essential fields set.
     * 
     * <p>Used for quick lesson creation in testing or when full lesson data
     * is not immediately available. The resulting entity requires additional
     * data before it can be published.</p>
     * 
     * @param title The lesson title
     * @param course The parent course
     * @param orderIndex The position of this lesson in the course
     * @return Minimal Lesson entity
     */
    public Lesson createMinimalLesson(String title, Course course, int orderIndex) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setSlug(generateLessonSlug(title, course.getSlug()));
        lesson.setCourse(course);
        lesson.setOrderIndex(orderIndex);
        lesson.setType(LessonType.VIDEO);
        lesson.setDurationInMinutes(0);
        lesson.setFree(false);
        lesson.setPublished(false);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        return lesson;
    }

    /**
     * Creates a summary response for lessons in course listings.
     * 
     * <p>Provides a lightweight lesson summary for course overview pages,
     * excluding heavy content URLs and detailed metadata to optimize
     * response size and loading performance.</p>
     * 
     * @param lesson The lesson entity to summarize
     * @return Lightweight LessonResponse DTO for listings
     */
    public LessonResponse toSummaryResponse(Lesson lesson) {
        if (lesson == null) {
            return null;
        }

        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setTitle(lesson.getTitle());
        response.setSlug(lesson.getSlug());
        response.setType(lesson.getType());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setDurationInMinutes(lesson.getDurationInMinutes());
        response.setFree(lesson.isFree());
        response.setPublished(lesson.isPublished());
        response.setHasVideo(lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty());
        response.setHasContent(lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty());

        return response;
    }

    /**
     * Checks if a lesson entity has all required fields for publication.
     * 
     * <p>Validates that a lesson has all necessary information before it can
     * be published and made available to students. Different lesson types
     * have different requirements.</p>
     * 
     * @param lesson The lesson entity to validate
     * @return true if lesson is ready for publication, false otherwise
     */
    public boolean isReadyForPublication(Lesson lesson) {
        if (lesson == null) {
            return false;
        }

        // Basic required fields
        boolean hasBasicFields = lesson.getTitle() != null && !lesson.getTitle().trim().isEmpty() &&
                               lesson.getDescription() != null && !lesson.getDescription().trim().isEmpty() &&
                               lesson.getType() != null &&
                               lesson.getDurationInMinutes() != null && lesson.getDurationInMinutes() > 0;

        if (!hasBasicFields) {
            return false;
        }

        // Type-specific requirements
        switch (lesson.getType()) {
            case VIDEO:
                return lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty();
            case TEXT:
                return lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty();
            case QUIZ:
            case ASSIGNMENT:
                return lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty();
            case RESOURCE:
                return lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty();
            default:
                return true;
        }
    }

    /**
     * Calculates the total duration for a list of lessons.
     * 
     * <p>Utility method for calculating course duration based on
     * individual lesson durations. Used in course summary calculations.</p>
     * 
     * @param lessons List of lessons to calculate duration for
     * @return Total duration in minutes
     */
    public int calculateTotalDuration(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            return 0;
        }

        return lessons.stream()
                .filter(lesson -> lesson.getDurationInMinutes() != null)
                .mapToInt(Lesson::getDurationInMinutes)
                .sum();
    }

    /**
     * Reorders lessons in a course to maintain sequential order indices.
     * 
     * <p>This method ensures that lessons in a course have sequential order indices
     * starting from 1, which is useful after lesson deletions or reordering operations.
     * The lessons are sorted by their current order index before reassigning new indices.</p>
     * 
     * @param lessons List of lessons to reorder
     * @return List of lessons with updated order indices
     */
    public List<Lesson> reorderLessons(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            return lessons;
        }

        // Sort lessons by current order index
        List<Lesson> sortedLessons = lessons.stream()
                .sorted((l1, l2) -> Integer.compare(l1.getOrderIndex(), l2.getOrderIndex()))
                .collect(Collectors.toList());

        // Reassign sequential order indices starting from 1
        IntStream.range(0, sortedLessons.size())
                .forEach(i -> {
                    Lesson lesson = sortedLessons.get(i);
                    lesson.setOrderIndex(i + 1);
                    lesson.setUpdatedAt(LocalDateTime.now());
                });

        return sortedLessons;
    }

    /**
     * Generates a unique slug for a lesson based on its title and course slug.
     * 
     * <p>Creates SEO-friendly URLs for lessons by combining the course slug
     * with a slugified version of the lesson title. The slug is guaranteed
     * to be URL-safe and unique within the course context.</p>
     * 
     * @param title The lesson title
     * @param courseSlug The parent course slug
     * @return Generated lesson slug
     */
    private String generateLessonSlug(String title, String courseSlug) {
        if (title == null || title.trim().isEmpty()) {
            return courseSlug + "-lesson-" + System.currentTimeMillis();
        }

        String lessonSlug = SlugUtils.generateSlug(title);
        return courseSlug + "-" + lessonSlug;
    }

    /**
     * Calculates the content size for a lesson based on its content URLs.
     * 
     * <p>Estimates the content size by analyzing the lesson's content URLs.
     * This is used for display purposes and bandwidth estimation. Returns
     * an approximate size in bytes.</p>
     * 
     * @param lesson The lesson entity
     * @return Estimated content size in bytes
     */
    private Long calculateContentSize(Lesson lesson) {
        if (lesson == null) {
            return 0L;
        }

        long totalSize = 0L;

        // Estimate video content size (approximate based on duration)
        if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty() 
            && lesson.getDurationInMinutes() != null && lesson.getDurationInMinutes() > 0) {
            // Rough estimate: 1 minute of video = 10MB (can be adjusted based on quality)
            totalSize += lesson.getDurationInMinutes() * 10 * 1024 * 1024L;
        }

        // Estimate content file size (default approximation)
        if (lesson.getContentUrl() != null && !lesson.getContentUrl().trim().isEmpty()) {
            // Default estimate for text/document content: 1MB
            totalSize += 1024 * 1024L;
        }

        // Estimate thumbnail size
        if (lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().trim().isEmpty()) {
            // Typical thumbnail size: 100KB
            totalSize += 100 * 1024L;
        }

        return totalSize;
    }

    /**
     * Creates a lesson copy with new identifiers for course duplication.
     * 
     * <p>Used when duplicating courses to create copies of lessons with
     * new IDs and timestamps but preserving all content and metadata.
     * The copy is created as unpublished by default.</p>
     * 
     * @param originalLesson The lesson to copy
     * @param targetCourse The course to associate the copy with
     * @return New lesson entity that is a copy of the original
     */
    public Lesson copyLesson(Lesson originalLesson, Course targetCourse) {
        if (originalLesson == null || targetCourse == null) {
            throw new IllegalArgumentException("Original lesson and target course cannot be null");
        }

        Lesson copy = new Lesson();
        copy.setTitle(originalLesson.getTitle());
        copy.setSlug(generateLessonSlug(originalLesson.getTitle(), targetCourse.getSlug()));
        copy.setDescription(originalLesson.getDescription());
        copy.setType(originalLesson.getType());
        copy.setOrderIndex(originalLesson.getOrderIndex());
        copy.setDurationInMinutes(originalLesson.getDurationInMinutes());
        copy.setVideoUrl(originalLesson.getVideoUrl());
        copy.setContentUrl(originalLesson.getContentUrl());
        copy.setThumbnailUrl(originalLesson.getThumbnailUrl());
        copy.setFree(originalLesson.isFree());
        copy.setPublished(false); // Copies start as unpublished
        copy.setCourse(targetCourse);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());

        return copy;
    }

    /**
     * Validates lesson data for consistency and business rules.
     * 
     * <p>Performs comprehensive validation of lesson data including
     * title length, content URLs, duration limits, and type-specific
     * validation rules.</p>
     * 
     * @param lesson The lesson entity to validate
     * @return true if lesson data is valid, false otherwise
     */
    public boolean validateLessonData(Lesson lesson) {
        if (lesson == null) {
            return false;
        }

        // Title validation
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty() || 
            lesson.getTitle().length() > 255) {
            return false;
        }

        // Description validation (optional but if present, should not be too long)
        if (lesson.getDescription() != null && lesson.getDescription().length() > 2000) {
            return false;
        }

        // Duration validation
        if (lesson.getDurationInMinutes() != null && 
            (lesson.getDurationInMinutes() < 0 || lesson.getDurationInMinutes() > 1440)) { // Max 24 hours
            return false;
        }

        // Order index validation
        if (lesson.getOrderIndex() != null && lesson.getOrderIndex() < 0) {
            return false;
        }

        // URL validation (basic check for valid URL format)
        if (!isValidUrl(lesson.getVideoUrl()) || 
            !isValidUrl(lesson.getContentUrl()) || 
            !isValidUrl(lesson.getThumbnailUrl())) {
            return false;
        }

        return true;
    }

    /**
     * Validates if a URL is properly formatted.
     * 
     * <p>Performs basic URL validation to ensure URLs are well-formed.
     * Null or empty URLs are considered valid (optional fields).</p>
     * 
     * @param url The URL to validate
     * @return true if URL is valid or null/empty, false otherwise
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Optional field
        }

        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    /**
     * Filters lessons based on publication status and access rights.
     * 
     * <p>Filters a list of lessons to return only those that should be
     * visible to users based on their access level and lesson publication status.</p>
     * 
     * @param lessons List of lessons to filter
     * @param includeUnpublished Whether to include unpublished lessons
     * @param includeNonFree Whether to include paid lessons
     * @return Filtered list of lessons
     */
    public List<Lesson> filterLessonsForAccess(List<Lesson> lessons, boolean includeUnpublished, boolean includeNonFree) {
        if (lessons == null) {
            return List.of();
        }

        return lessons.stream()
                .filter(lesson -> includeUnpublished || lesson.isPublished())
                .filter(lesson -> includeNonFree || lesson.isFree())
                .collect(Collectors.toList());
    }

    /**
     * Counts lessons by type in a course.
     * 
     * <p>Provides statistics about lesson type distribution in a course,
     * useful for course analytics and reporting.</p>
     * 
     * @param lessons List of lessons to analyze
     * @return Map of lesson types to their counts
     */
    public java.util.Map<LessonType, Long> countLessonsByType(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            return java.util.Map.of();
        }

        return lessons.stream()
                .collect(Collectors.groupingBy(
                    Lesson::getType,
                    Collectors.counting()
                ));
    }
}