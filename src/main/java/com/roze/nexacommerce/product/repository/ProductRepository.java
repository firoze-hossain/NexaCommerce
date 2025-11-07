package com.roze.nexacommerce.product.repository;

import com.roze.nexacommerce.brand.entity.Brand;
import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    List<Product> findByVendor(VendorProfile vendor);

    Page<Product> findByVendor(VendorProfile vendor, Pageable pageable);

    List<Product> findByCategory(Category category);

    Page<Product> findByCategory(Category category, Pageable pageable);

    List<Product> findByStatus(ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    List<Product> findByFeaturedTrueAndPublishedTrueAndStatus(ProductStatus status);

    List<Product> findByPublishedTrueAndStatus(ProductStatus status);

    Page<Product> findByPublishedTrueAndStatus(ProductStatus status, Pageable pageable);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("SELECT p FROM Product p WHERE p.vendor = :vendor AND p.published = true AND p.status = 'ACTIVE'")
    List<Product> findActiveProductsByVendor(VendorProfile vendor);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.published = true AND p.status = 'ACTIVE'")
    Page<Product> findActiveProductsByCategory(Category category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.published = true AND p.status = 'ACTIVE'")
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND p.published = true AND p.status = 'ACTIVE'")
    Page<Product> searchProducts(String query, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.vendor = :vendor")
    Long countByVendor(VendorProfile vendor);

    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.published = true AND p.status = 'ACTIVE'")
    Page<Product> findActiveProductsByBrand(Brand brand, Pageable pageable);


//    @Query("SELECT p FROM Product p WHERE " +
//            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
//            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
//            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')) " +
//            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))) " +
//            "AND p.published = true AND p.status = 'ACTIVE' " +
//            "AND (:categories IS NULL OR p.category.id IN :categories) " +
//            "AND (:brands IS NULL OR p.brand.id IN :brands) " +
//            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
//            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
//            "AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock <= 0)) " +
//            "AND (:featured IS NULL OR p.featured = :featured)")
//    Page<Product> searchWithFilters(
//            @Param("query") String query,
//            @Param("categories") List<Long> categories,
//            @Param("brands") List<Long> brands,
//            @Param("minPrice") BigDecimal minPrice,
//            @Param("maxPrice") BigDecimal maxPrice,
//            @Param("inStock") Boolean inStock,
//            @Param("featured") Boolean featured,
//            Pageable pageable);
@Query("SELECT p FROM Product p WHERE " +
        "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%'))) " + // ADD THIS LINE
        "AND p.published = true AND p.status = 'ACTIVE' " +
        "AND (:categories IS NULL OR p.category.id IN :categories) " +
        "AND (:brands IS NULL OR p.brand.id IN :brands) " +
        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
        "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
        "AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock <= 0)) " +
        "AND (:featured IS NULL OR p.featured = :featured)")
Page<Product> searchWithFilters(
        @Param("query") String query,
        @Param("categories") List<Long> categories,
        @Param("brands") List<Long> brands,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("inStock") Boolean inStock,
        @Param("featured") Boolean featured,
        Pageable pageable);
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND p.published = true AND p.status = 'ACTIVE' " +
            "ORDER BY p.name ASC")
    List<Product> searchProductsAutocomplete(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT DISTINCT p.name FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND p.published = true AND p.status = 'ACTIVE' " +
            "ORDER BY p.name ASC")
    List<String> findProductNameSuggestions(@Param("query") String query, @Param("limit") int limit);
}