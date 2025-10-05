package com.roze.nexacommerce.category.repository;

import com.roze.nexacommerce.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}