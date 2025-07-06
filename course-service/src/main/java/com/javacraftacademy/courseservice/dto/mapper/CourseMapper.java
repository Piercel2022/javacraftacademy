
package com.javacraftacademy.courseservice.dto.mapper;

import com.javacraftacademy.courseservice.dto.request.CreateCourseRequest;
import com.javacraftacademy.courseservice.dto.request.UpdateCourseRequest;
import com.javacraftacademy.courseservice.dto.response.CourseResponse;
import com.javacraftacademy.courseservice.dto.response.CourseDetailResponse;
import com.javacraftacademy.courseservice.model.entity.Course;
import com.javacraftacademy.courseservice.model.entity.Category;
import com.javacraftacademy.courseservice.model.entity.Lesson;
import com.javacraftacademy.courseservice.model.enums.CourseStatus;
import com.javacraftacademy.courseservice.model.enums.CourseLevel;
import com.javacraftacademy.courseservice.util.SlugUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component responsible for converting between Course entities and DTOs.
 * 
 * <p>This mapper handles the transformation of course data between different layers
 * of the application, ensuring proper data mapping and maintaining relationships
 * with other entities like Category, Lesson, and Enrollment.</p>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Convert Course entities to response DTOs for API responses</li>
 *   <li>Transform request DTOs to Course entities for persistence</li>
 *   <li>Handle complex mappings including nested relationships</li>
 *   <li>Maintain data integrity during transformations</li>
 *   <li>Generate course slugs for SEO-friendly URLs</li>
 * </ul>
 * 
 * <h3>Relationships with Application Components:</h3>
 * <ul>
 *   <li><strong>Controllers:</strong> Used by CourseController to transform request/response data</li>
 *   <li><strong>Services:</strong> Utilized by CourseService for data transformation</li>
 *   <li><strong>Entities:</strong> Maps Course, Category, and Lesson entities</li>
 *   <li><strong>DTOs:</strong> Transforms between request/response DTOs and entities</li>
 *   <li><strong>Utilities:</strong> Uses SlugUtils for URL-friendly slug generation</li>
 * </ul>
 * 
 * @author JavaCraftAcademy
 * @version 1.0
 * @since 1.0
 */
@Component
public class CourseMapper {

    private final CategoryMapper categoryMapper;
    private final LessonMapper lessonMapper;

    /**
     * Constructor with dependency injection for related mappers.
     * 
     * @param categoryMapper The category mapper for handling category transformations
     * @param lessonMapper The lesson mapper for handling lesson transformations
     */
    public CourseMapper(CategoryMapper categoryMapper, LessonMapper lessonMapper) {
        this.categoryMapper = categoryMapper;
        this.lessonMapper = lessonMapper;
    }

    /**
     * Converts a Course entity to a CourseResponse DTO for API responses.
     * 
     * <p>This method creates a simplified view of the course suitable for
     * list views and basic course information display. It includes essential
     * course details but excludes heavy nested data like full lesson content.</p>
     * 
     * @param course The course entity to convert
     * @return CourseResponse DTO containing essential course information
     * @throws IllegalArgumentException if course is null
     */
    public CourseResponse toResponse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setSlug(course.getSlug());
        response.setDescription(course.getDescription());
        response.setShortDescription(course.getShortDescription());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setPrice(course.getPrice());
        response.setDiscountPrice(course.getDiscountPrice());
        response.setLevel(course.getLevel());
        response.setStatus(course.getStatus());
        response.setDurationInHours(course.getDurationInHours());
        response.setInstructorId(course.getInstructorId());
        response.setInstructorName(course.getInstructorName());
        response.setEnrollmentCount(course.getEnrollmentCount());
        response.setRating(course.getRating());
        response.setReviewCount(course.getReviewCount());
        response.setLanguage(course.getLanguage());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());

        // Map category if present
        if (course.getCategory() != null) {
            response.setCategory(categoryMapper.toResponse(course.getCategory()));
        }

        // Map lesson count
        if (course.getLessons() != null) {
            response.setLessonCount(course.getLessons().size());
        }

        return response;
    }

    /**
     * Converts a Course entity to a detailed CourseDetailResponse DTO.
     * 
     * <p>This method creates a comprehensive view of the course including
     * all nested relationships such as lessons, category details, and
     * enrollment information. Used for detailed course views and course
     * management interfaces.</p>
     * 
     * @param course The course entity to convert
     * @return CourseDetailResponse DTO containing complete course information
     * @throws IllegalArgumentException if course is null
     */
    public CourseDetailResponse toDetailResponse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        CourseDetailResponse response = new CourseDetailResponse();
        
        // Copy basic course information
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setSlug(course.getSlug());
        response.setDescription(course.getDescription());
        response.setShortDescription(course.getShortDescription());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setPrice(course.getPrice());
        response.setDiscountPrice(course.getDiscountPrice());
        response.setLevel(course.getLevel());
        response.setStatus(course.getStatus());
        response.setDurationInHours(course.getDurationInHours());
        response.setInstructorId(course.getInstructorId());
        response.setInstructorName(course.getInstructorName());
        response.setEnrollmentCount(course.getEnrollmentCount());
        response.setRating(course.getRating());
        response.setReviewCount(course.getReviewCount());
        response.setLanguage(course.getLanguage());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());

        // Map category details
        if (course.getCategory() != null) {
            response.setCategory(categoryMapper.toResponse(course.getCategory()));
        }

        // Map lessons with full details
        if (course.getLessons() != null) {
            response.setLessons(course.getLessons().stream()
                    .map(lessonMapper::toResponse)
                    .collect(Collectors.toList()));
        }

        // Additional metadata for detailed view
        response.setPrerequisites(course.getPrerequisites());
        response.setLearningObjectives(course.getLearningObjectives());
        response.setTargetAudience(course.getTargetAudience());
        response.setCertificateProvided(course.isCertificateProvided());

        return response;
    }

    /**
     * Converts a CreateCourseRequest DTO to a Course entity.
     * 
     * <p>This method transforms course creation requests into new Course entities,
     * setting default values and generating required fields like slugs and timestamps.
     * The created entity is ready for persistence but requires additional processing
     * for relationships and validation.</p>
     * 
     * @param request The course creation request DTO
     * @return New Course entity ready for persistence
     * @throws IllegalArgumentException if request is null or invalid
     */
    public Course toEntity(CreateCourseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateCourseRequest cannot be null");
        }

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setSlug(SlugUtils.generateSlug(request.getTitle()));
        course.setDescription(request.getDescription());
        course.setShortDescription(request.getShortDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(request.getPrice());
        course.setDiscountPrice(request.getDiscountPrice());
        course.setLevel(request.getLevel() != null ? request.getLevel() : CourseLevel.BEGINNER);
        course.setStatus(CourseStatus.DRAFT); // New courses start as draft
        course.setDurationInHours(request.getDurationInHours());
        course.setInstructorId(request.getInstructorId());
        course.setInstructorName(request.getInstructorName());
        course.setLanguage(request.getLanguage() != null ? request.getLanguage() : "English");
        course.setPrerequisites(request.getPrerequisites());
        course.setLearningObjectives(request.getLearningObjectives());
        course.setTargetAudience(request.getTargetAudience());
        course.setCertificateProvided(request.isCertificateProvided());

        // Set default values
        course.setEnrollmentCount(0);
        course.setRating(0.0);
        course.setReviewCount(0);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return course;
    }

    /**
     * Updates an existing Course entity with data from UpdateCourseRequest DTO.
     * 
     * <p>This method selectively updates course fields based on the update request,
     * preserving existing data where not specified in the request. It handles
     * partial updates and maintains data integrity during the update process.</p>
     * 
     * @param course The existing course entity to update
     * @param request The update request containing new values
     * @throws IllegalArgumentException if course or request is null
     */
    public void updateEntity(Course course, UpdateCourseRequest request) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateCourseRequest cannot be null");
        }

        // Update fields only if provided in request
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
            course.setSlug(SlugUtils.generateSlug(request.getTitle()));
        }
        
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        
        if (request.getShortDescription() != null) {
            course.setShortDescription(request.getShortDescription());
        }
        
        if (request.getThumbnailUrl() != null) {
            course.setThumbnailUrl(request.getThumbnailUrl());
        }
        
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        
        if (request.getDiscountPrice() != null) {
            course.setDiscountPrice(request.getDiscountPrice());
        }
        
        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }
        
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        
        if (request.getDurationInHours() != null) {
            course.setDurationInHours(request.getDurationInHours());
        }
        
        if (request.getLanguage() != null) {
            course.setLanguage(request.getLanguage());
        }
        
        if (request.getPrerequisites() != null) {
            course.setPrerequisites(request.getPrerequisites());
        }
        
        if (request.getLearningObjectives() != null) {
            course.setLearningObjectives(request.getLearningObjectives());
        }
        
        if (request.getTargetAudience() != null) {
            course.setTargetAudience(request.getTargetAudience());
        }
        
        if (request.getCertificateProvided() != null) {
            course.setCertificateProvided(request.getCertificateProvided());
        }

        // Always update the modification timestamp
        course.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Converts a list of Course entities to a list of CourseResponse DTOs.
     * 
     * <p>Utility method for bulk transformation of course entities to response DTOs.
     * Commonly used in list endpoints and search results.</p>
     * 
     * @param courses List of course entities to convert
     * @return List of CourseResponse DTOs
     */
    public List<CourseResponse> toResponseList(List<Course> courses) {
        if (courses == null) {
            return List.of();
        }
        
        return courses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a minimal Course entity with only essential fields set.
     * 
     * <p>Used for quick course creation in testing or when full course data
     * is not immediately available. The resulting entity requires additional
     * data before it can be persisted.</p>
     * 
     * @param title The course title
     * @param instructorId The instructor identifier
     * @return Minimal Course entity
     */
    public Course createMinimalCourse(String title, Long instructorId) {
        Course course = new Course();
        course.setTitle(title);
        course.setSlug(SlugUtils.generateSlug(title));
        course.setInstructorId(instructorId);
        course.setStatus(CourseStatus.DRAFT);
        course.setLevel(CourseLevel.BEGINNER);
        course.setEnrollmentCount(0);
        course.setRating(0.0);
        course.setReviewCount(0);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }

    /**
     * Checks if a course entity has all required fields for publication.
     * 
     * <p>Validates that a course has all necessary information before it can
     * be published and made available to students. Used in course publishing workflows.</p>
     * 
     * @param course The course entity to validate
     * @return true if course is ready for publication, false otherwise
     */
    public boolean isReadyForPublication(Course course) {
        if (course == null) {
            return false;
        }

        return course.getTitle() != null && !course.getTitle().trim().isEmpty() &&
               course.getDescription() != null && !course.getDescription().trim().isEmpty() &&
               course.getShortDescription() != null && !course.getShortDescription().trim().isEmpty() &&
               course.getThumbnailUrl() != null && !course.getThumbnailUrl().trim().isEmpty() &&
               course.getPrice() != null &&
               course.getLevel() != null &&
               course.getInstructorId() != null &&
               course.getInstructorName() != null && !course.getInstructorName().trim().isEmpty() &&
               course.getLessons() != null && !course.getLessons().isEmpty();
    }
}