package com.roze.nexacommerce.product.service.impl;

import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.category.repository.CategoryRepository;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.product.dto.request.ProductCreateRequest;
import com.roze.nexacommerce.product.dto.request.ProductUpdateRequest;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.product.mapper.ProductMapper;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.product.service.ProductService;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, Long vendorId) {
        // Validate unique constraint
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Get vendor
        VendorProfile vendor = null;
        if (vendorId != null) {
            vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
        }


        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = productMapper.toEntity(request);
        product.setVendor(vendor);
        product.setCategory(category);

        // Set images and attributes relationships
        product.getImages().forEach(image -> image.setProduct(product));
        product.getAttributes().forEach(attribute -> attribute.setProduct(product));

        Product savedProduct = productRepository.save(product);

        // Update vendor product count
        if (vendorId != null) {
            vendor.addProduct();
            vendorRepository.save(vendor);
        }
        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return productMapper.toResponse(product);
    }

    @Override
    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public PaginatedResponse<ProductResponse> getProductsByVendor(Long vendorId, Pageable pageable) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        Page<Product> productPage = productRepository.findByVendor(vendor, pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public PaginatedResponse<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public PaginatedResponse<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(status, pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public PaginatedResponse<ProductResponse> searchProducts(String query, Pageable pageable) {
        Page<Product> productPage = productRepository.searchProducts(query, pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public PaginatedResponse<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return buildPaginatedResponse(productPage);
    }

    @Override
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedTrueAndPublishedTrueAndStatus(ProductStatus.ACTIVE);
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Validate unique constraint if SKU is being updated
        if (request.getSku() != null && !request.getSku().equals(product.getSku())
                && productRepository.existsBySkuAndIdNot(request.getSku(), productId)) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        productMapper.updateEntity(request, product);

        // Update images and attributes
        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(imageRequest -> {
                var image = productMapper.toImageEntity(imageRequest);
                image.setProduct(product);
                product.getImages().add(image);
            });
        }

        if (request.getAttributes() != null) {
            product.getAttributes().clear();
            request.getAttributes().forEach(attributeRequest -> {
                var attribute = productMapper.toAttributeEntity(attributeRequest);
                attribute.setProduct(product);
                product.getAttributes().add(attribute);
            });
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long productId, ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStock(Long productId, Integer stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setStock(stock);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Update vendor product count
        VendorProfile vendor = product.getVendor();
        vendor.removeProduct();
        vendorRepository.save(vendor);

        productRepository.delete(product);
    }

    private PaginatedResponse<ProductResponse> buildPaginatedResponse(Page<Product> productPage) {
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<ProductResponse>builder()
                .items(productResponses)
                .totalItems(productPage.getTotalElements())
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .build();
    }
}