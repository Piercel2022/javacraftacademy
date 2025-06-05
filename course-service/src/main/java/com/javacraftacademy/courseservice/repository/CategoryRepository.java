// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/CategoryRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByParentIdIsNull();
    
    List<Category> findByParentId(Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.active = true")
    List<Category> findActiveCategories();
    
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parentId IS NULL")
    List<Category> findActiveRootCategories();
    
    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parentId = :parentId")
    List<Category> findActiveSubcategories(@Param("parentId") Long parentId);
    
    @Query("SELECT COUNT(course) FROM Course course WHERE course.category.id = :categoryId")
    Long countCoursesByCategoryId(@Param("categoryId") Long categoryId);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Category> searchByKeyword(@Param("keyword") String keyword);
    
    boolean existsBySlug(String slug);
    
    boolean existsByNameAndParentId(String name, Long parentId);
}
