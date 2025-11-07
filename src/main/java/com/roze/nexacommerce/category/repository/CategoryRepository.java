package com.roze.nexacommerce.category.repository;

import com.roze.nexacommerce.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByParentIsNull();
    
    List<Category> findByParentId(Long parentId);
    
    List<Category> findByActiveTrue();
    
    List<Category> findByFeaturedTrueAndActiveTrue();
    
    Page<Category> findByActiveTrue(Pageable pageable);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true")
    List<Category> findRootCategories();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category AND p.published = true AND p.status = 'ACTIVE'")
    Long countActiveProductsByCategory(Category category);

    @Query("SELECT c FROM Category c WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND c.active = true")
    List<Category> searchCategories(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND c.active = true " +
            "ORDER BY c.name ASC")
    List<Category> searchCategoriesAutocomplete(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT DISTINCT c.name FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND c.active = true " +
            "ORDER BY c.name ASC")
    List<String> findCategoryNameSuggestions(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds AND c.active = true")
    List<Category> findActiveCategoriesByIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.displayOrder")
    List<Category> findActiveRootCategories();

    Collection<Category> findByIdIn(Set<Long> availableCategoryIds);
}