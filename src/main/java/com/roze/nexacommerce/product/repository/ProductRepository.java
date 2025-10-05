package com.roze.nexacommerce.product.repository;

import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
}