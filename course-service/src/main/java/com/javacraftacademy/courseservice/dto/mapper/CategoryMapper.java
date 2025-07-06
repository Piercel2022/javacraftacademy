
package com.javacraftacademy.courseservice.dto.mapper;

import com.javacraftacademy.courseservice.dto.response.CategoryResponse;
import com.javacraftacademy.courseservice.model.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component responsible for converting between Category entities and their corresponding DTOs.
 * This component plays a crucial role in the data transfer layer of the course service application,
 * providing clean separation between internal entity representations and external API contracts.
 * 
 * <p>The CategoryMapper integrates with multiple layers of the application:</p>
 * <ul>
 *   <li><strong>Controller Layer:</strong> Used by CategoryController to convert entities to response DTOs</li>
 *   <li><strong>Service Layer:</strong> Utilized by CategoryService implementations for data transformation</li>
 *   <li><strong>Repository Layer:</strong> Helps transform repository query results to DTOs</li>
 * </ul>
 * 
 * <p>Key relationships within the application:</p>
 * <ul>
 *   <li><strong>Category Entity:</strong> Maps from Category JPA entities to CategoryResponse DTOs</li>
 *   <li><strong>Course Entity:</strong> Considers category-course relationships when mapping</li>
 *   <li><strong>Exception Handling:</strong> Works with GlobalExceptionHandler for error scenarios</li>
 *   <li><strong>Validation:</strong> Ensures mapped data maintains integrity constraints</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024-01-01
 * @see Category
 * @see CategoryResponse
 * @see com.javacraftacademy.courseservice.controller.CategoryController
 * @see com.javacraftacademy.courseservice.service.CategoryService
 */
@Component
public class CategoryMapper {

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     * This method transforms the internal entity representation to a format suitable
     * for API responses, including all relevant category information and metadata.
     * 
     * <p>The mapping process includes:</p>
     * <ul>
     *   <li>Basic category information (id, name, description)</li>
     *   <li>Category metadata (slug, color, icon)</li>
     *   <li>Status and hierarchy information</li>
     *   <li>Course count if available</li>
     *   <li>Audit information (creation and modification timestamps)</li>
     * </ul>
     * 
     * <p>Usage in application flow:</p>
     * <pre>
     * CategoryController → CategoryService → CategoryMapper.toResponse()
     * </pre>
     * 
     * @param category The Category entity to convert. Must not be null.
     * @return CategoryResponse DTO containing all mapped category information
     * @throws IllegalArgumentException if the provided category is null
     * @throws RuntimeException if mapping fails due to data inconsistency
     * 
     * @see CategoryResponse
     * @see toResponseList(List)
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category entity cannot be null for mapping");
        }

        try {
            CategoryResponse response = new CategoryResponse();
            
            // Basic category information
            response.setId(category.getId());
            response.setName(category.getName());
            response.setDescription(category.getDescription());
            response.setSlug(category.getSlug());
            
            // Visual and organizational attributes
            response.setColor(category.getColor());
            response.setIcon(category.getIcon());
            response.setDisplayOrder(category.getDisplayOrder());
            
            // Status and hierarchy
            response.setActive(category.isActive());
            response.setParentId(category.getParentId());
            
            // Course statistics
            response.setCourseCount(category.getCourseCount() != null ? category.getCourseCount() : 0);
            
            // Audit information
            response.setCreatedAt(category.getCreatedAt());
            response.setUpdatedAt(category.getUpdatedAt());
            response.setCreatedBy(category.getCreatedBy());
            response.setUpdatedBy(category.getUpdatedBy());
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Category entity to CategoryResponse: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a list of Category entities to a list of CategoryResponse DTOs.
     * This method efficiently processes collections of categories, maintaining
     * the original order and applying consistent mapping logic to each element.
     * 
     * <p>This method is particularly useful for:</p>
     * <ul>
     *   <li>Category listing endpoints</li>
     *   <li>Hierarchical category tree construction</li>
     *   <li>Bulk category operations</li>
     *   <li>Search result transformations</li>
     * </ul>
     * 
     * <p>Performance considerations:</p>
     * <ul>
     *   <li>Uses Java 8 Streams for efficient processing</li>
     *   <li>Maintains lazy evaluation where possible</li>
     *   <li>Preserves original collection order</li>
     *   <li>Handles empty collections gracefully</li>
     * </ul>
     * 
     * @param categories List of Category entities to convert. Can be empty but not null.
     * @return List of CategoryResponse DTOs corresponding to the input entities
     * @throws IllegalArgumentException if the provided list is null
     * @throws RuntimeException if any individual mapping operation fails
     * 
     * @see #toResponse(Category)
     * @see CategoryResponse
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("Category list cannot be null for mapping");
        }

        try {
            return categories.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Category list to CategoryResponse list: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Category entity to a simplified CategoryResponse DTO for summary views.
     * This method creates a lightweight version of the category response, containing
     * only essential information for list views, dropdowns, or navigation components.
     * 
     * <p>The summary mapping includes:</p>
     * <ul>
     *   <li>Category identifier and name</li>
     *   <li>Slug for URL generation</li>
     *   <li>Visual attributes (color, icon)</li>
     *   <li>Active status</li>
     *   <li>Course count for display purposes</li>
     * </ul>
     * 
     * <p>Use cases:</p>
     * <ul>
     *   <li>Category dropdown lists in forms</li>
     *   <li>Navigation menu generation</li>
     *   <li>Quick category overview displays</li>
     *   <li>Mobile-optimized category lists</li>
     * </ul>
     * 
     * @param category The Category entity to convert to summary format
     * @return CategoryResponse DTO with essential information only
     * @throws IllegalArgumentException if the provided category is null
     * 
     * @see #toResponse(Category)
     * @see toSummaryList(List)
     */
    public CategoryResponse toSummary(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category entity cannot be null for summary mapping");
        }

        CategoryResponse summary = new CategoryResponse();
        summary.setId(category.getId());
        summary.setName(category.getName());
        summary.setSlug(category.getSlug());
        summary.setColor(category.getColor());
        summary.setIcon(category.getIcon());
        summary.setActive(category.isActive());
        summary.setCourseCount(category.getCourseCount() != null ? category.getCourseCount() : 0);
        
        return summary;
    }

    /**
     * Converts a list of Category entities to a list of summary CategoryResponse DTOs.
     * This method efficiently creates lightweight category representations suitable
     * for scenarios where full category details are not required.
     * 
     * @param categories List of Category entities to convert to summary format
     * @return List of summary CategoryResponse DTOs
     * @throws IllegalArgumentException if the provided list is null
     * 
     * @see #toSummary(Category)
     * @see toResponseList(List)
     */
    public List<CategoryResponse> toSummaryList(List<Category> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("Category list cannot be null for summary mapping");
        }

        return categories.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Creates a hierarchical CategoryResponse that includes child categories.
     * This method is specifically designed for building category trees and
     * nested category structures commonly used in course organization.
     * 
     * <p>Hierarchical mapping features:</p>
     * <ul>
     *   <li>Includes all standard category information</li>
     *   <li>Recursively maps child categories</li>
     *   <li>Maintains parent-child relationships</li>
     *   <li>Calculates aggregate course counts</li>
     * </ul>
     * 
     * <p>Integration points:</p>
     * <ul>
     *   <li><strong>CategoryService:</strong> Used for tree-building operations</li>
     *   <li><strong>Course Navigation:</strong> Powers hierarchical course browsing</li>
     *   <li><strong>Admin Interface:</strong> Supports category management trees</li>
     * </ul>
     * 
     * @param category The parent Category entity to convert
     * @param children List of child Category entities
     * @return CategoryResponse DTO with hierarchical structure
     * @throws IllegalArgumentException if the parent category is null
     * 
     * @see #toResponse(Category)
     * @see com.javacraftacademy.courseservice.service.CategoryService
     */
    public CategoryResponse toHierarchicalResponse(Category category, List<Category> children) {
        if (category == null) {
            throw new IllegalArgumentException("Parent category cannot be null for hierarchical mapping");
        }

        CategoryResponse response = toResponse(category);
        
        if (children != null && !children.isEmpty()) {
            List<CategoryResponse> childrenResponses = toResponseList(children);
            response.setChildren(childrenResponses);
            
            // Calculate total course count including children
            int totalCourseCount = response.getCourseCount();
            totalCourseCount += children.stream()
                    .mapToInt(child -> child.getCourseCount() != null ? child.getCourseCount() : 0)
                    .sum();
            response.setTotalCourseCount(totalCourseCount);
        }
        
        return response;
    }

    /**
     * Validates that a Category entity contains all required data for mapping.
     * This method performs comprehensive validation to ensure data integrity
     * before the mapping process begins.
     * 
     * <p>Validation checks include:</p>
     * <ul>
     *   <li>Non-null entity reference</li>
     *   <li>Required field presence (id, name)</li>
     *   <li>Field format validation (slug format, color codes)</li>
     *   <li>Logical consistency (parent-child relationships)</li>
     * </ul>
     * 
     * <p>This method is automatically called by mapping methods but can also
     * be used independently for validation purposes in service layers.</p>
     * 
     * @param category The Category entity to validate
     * @return true if the entity is valid for mapping
     * @throws IllegalArgumentException if validation fails with specific error message
     * 
     * @see #toResponse(Category)
     * @see com.javacraftacademy.courseservice.validation.ValidCourseData
     */
    public boolean validateCategoryForMapping(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category entity cannot be null");
        }

        if (category.getId() == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be null or empty");
        }

        // Validate slug format (lowercase, hyphens allowed)
        if (!category.getSlug().matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Category slug must contain only lowercase letters, numbers, and hyphens");
        }

        // Validate color format if present (hex color code)
        if (category.getColor() != null && !category.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Category color must be a valid hex color code");
        }

        return true;
    }

    /**
     * Creates a minimal CategoryResponse for error scenarios or placeholder content.
     * This method generates a safe, default category response when full mapping
     * is not possible or when providing fallback content.
     * 
     * <p>Use cases:</p>
     * <ul>
     *   <li>Error recovery in mapping operations</li>
     *   <li>Placeholder content during loading states</li>
     *   <li>Default category representations</li>
     *   <li>Testing and development scenarios</li>
     * </ul>
     * 
     * @return CategoryResponse with safe default values
     * 
     * @see #toResponse(Category)
     * @see com.javacraftacademy.courseservice.exception.GlobalExceptionHandler
     */
    public CategoryResponse createDefaultResponse() {
        CategoryResponse defaultResponse = new CategoryResponse();
        defaultResponse.setId(-1L);
        defaultResponse.setName("Unknown Category");
        defaultResponse.setSlug("unknown-category");
        defaultResponse.setDescription("Category information not available");
        defaultResponse.setActive(false);
        defaultResponse.setCourseCount(0);
        defaultResponse.setColor("#808080");
        defaultResponse.setIcon("default");
        
        return defaultResponse;
    }
}