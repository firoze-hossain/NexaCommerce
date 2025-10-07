package com.roze.nexacommerce.brand.repository;

import com.roze.nexacommerce.brand.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    Optional<Brand> findByName(String name);
    
    Optional<Brand> findBySlug(String slug);
    
    List<Brand> findByActiveTrue();
    
    List<Brand> findByFeaturedTrueAndActiveTrue();
    
    Page<Brand> findByActiveTrue(Pageable pageable);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT b FROM Brand b WHERE b.active = true ORDER BY b.name")
    List<Brand> findAllActiveBrands();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand = :brand AND p.published = true")
    Long countActiveProductsByBrand(Brand brand);
}