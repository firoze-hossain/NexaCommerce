package com.roze.nexacommerce.brand.repository;

import com.roze.nexacommerce.brand.entity.Brand;
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

    @Query("SELECT b FROM Brand b WHERE " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND b.active = true")
    List<Brand> searchBrands(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT b FROM Brand b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND b.active = true " +
            "ORDER BY b.name ASC")
    List<Brand> searchBrandsAutocomplete(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT DISTINCT b.name FROM Brand b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND b.active = true " +
            "ORDER BY b.name ASC")
    List<String> findBrandNameSuggestions(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT b FROM Brand b WHERE b.id IN :brandIds AND b.active = true")
    List<Brand> findActiveBrandsByIds(@Param("brandIds") List<Long> brandIds);

    Collection<Brand> findByIdIn(Set<Long> availableBrandIds);
}