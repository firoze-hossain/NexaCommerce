package com.roze.nexacommerce.product.service.impl;

import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.category.repository.CategoryRepository;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.product.dto.request.ProductAttributeRequest;
import com.roze.nexacommerce.product.dto.request.ProductCreateRequest;
import com.roze.nexacommerce.product.dto.request.ProductImageRequest;
import com.roze.nexacommerce.product.dto.request.ProductUpdateRequest;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.entity.ProductAttribute;
import com.roze.nexacommerce.product.entity.ProductImage;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.product.mapper.ProductMapper;
import com.roze.nexacommerce.product.repository.ProductAttributeRepository;
import com.roze.nexacommerce.product.repository.ProductImageRepository;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.product.service.ProductService;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductImageRepository productImageRepository;

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

        // Update basic fields
        productMapper.updateEntity(request, product);

        // ========== SMART UPDATE FOR IMAGES ==========
        if (request.getImages() != null) {
            updateProductImages(product, request.getImages());
        }

        // ========== SMART UPDATE FOR ATTRIBUTES ==========
        if (request.getAttributes() != null) {
            updateProductAttributes(product, request.getAttributes());
        }

        // Save the main product entity
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", productId);
        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Smart update for product images:
     * - Images with ID: Update existing images
     * - Images without ID: Add new images
     * - Missing existing IDs: Remove those images
     */
    private void updateProductImages(Product product, List<ProductImageRequest> imageRequests) {
        // Convert request to entities and set product reference
        List<ProductImage> newImages = imageRequests.stream()
                .map(request -> {
                    ProductImage image = productMapper.toImageEntity(request);
                    image.setProduct(product);
                    return image;
                })
                .collect(Collectors.toList());

        // Separate new images (without ID) and existing images (with ID)
        List<ProductImage> imagesToAdd = newImages.stream()
                .filter(img -> img.getId() == null)
                .collect(Collectors.toList());

        Map<Long, ProductImage> imagesToUpdate = newImages.stream()
                .filter(img -> img.getId() != null)
                .collect(Collectors.toMap(ProductImage::getId, img -> img));

        // Get current images
        List<ProductImage> currentImages = new ArrayList<>(product.getImages());

        // Update existing images
        for (ProductImage currentImage : currentImages) {
            if (imagesToUpdate.containsKey(currentImage.getId())) {
                ProductImage updatedImage = imagesToUpdate.get(currentImage.getId());
                // Update fields of existing image
                currentImage.setImageUrl(updatedImage.getImageUrl());
                currentImage.setAltText(updatedImage.getAltText());
                currentImage.setDisplayOrder(updatedImage.getDisplayOrder());
                currentImage.setIsPrimary(updatedImage.getIsPrimary());
            }
        }

        // Remove images that are not in the request (if frontend wants to delete specific ones)
        // This assumes frontend sends ALL images they want to keep + new ones
        Set<Long> requestedImageIds = imageRequests.stream()
                .map(req -> req.getId()) // You need to add id field to ProductImageRequest
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ProductImage> imagesToRemove = currentImages.stream()
                .filter(img -> !requestedImageIds.contains(img.getId()))
                .collect(Collectors.toList());

        imagesToRemove.forEach(product::removeImage);

        // Add new images
        imagesToAdd.forEach(product::addImage);
    }

    /**
     * Smart update for product attributes:
     * - Attributes with ID: Update existing attributes
     * - Attributes without ID: Add new attributes
     * - Missing existing IDs: Remove those attributes
     */
    private void updateProductAttributes(Product product, List<ProductAttributeRequest> attributeRequests) {
        // Convert request to entities and set product reference
        List<ProductAttribute> newAttributes = attributeRequests.stream()
                .map(request -> {
                    ProductAttribute attribute = productMapper.toAttributeEntity(request);
                    attribute.setProduct(product);
                    return attribute;
                })
                .collect(Collectors.toList());

        // Separate new attributes (without ID) and existing attributes (with ID)
        List<ProductAttribute> attributesToAdd = newAttributes.stream()
                .filter(attr -> attr.getId() == null)
                .collect(Collectors.toList());

        Map<Long, ProductAttribute> attributesToUpdate = newAttributes.stream()
                .filter(attr -> attr.getId() != null)
                .collect(Collectors.toMap(ProductAttribute::getId, attr -> attr));

        // Get current attributes
        List<ProductAttribute> currentAttributes = new ArrayList<>(product.getAttributes());

        // Update existing attributes
        for (ProductAttribute currentAttribute : currentAttributes) {
            if (attributesToUpdate.containsKey(currentAttribute.getId())) {
                ProductAttribute updatedAttribute = attributesToUpdate.get(currentAttribute.getId());
                // Update fields of existing attribute
                currentAttribute.setName(updatedAttribute.getName());
                currentAttribute.setValue(updatedAttribute.getValue());
                currentAttribute.setDisplayType(updatedAttribute.getDisplayType());
                currentAttribute.setDisplayOrder(updatedAttribute.getDisplayOrder());
            }
        }

        // Remove attributes that are not in the request
        Set<Long> requestedAttributeIds = attributeRequests.stream()
                .map(req -> req.getId()) // You need to add id field to ProductAttributeRequest
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ProductAttribute> attributesToRemove = currentAttributes.stream()
                .filter(attr -> !requestedAttributeIds.contains(attr.getId()))
                .collect(Collectors.toList());

        attributesToRemove.forEach(product::removeAttribute);

        // Add new attributes
        attributesToAdd.forEach(product::addAttribute);
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

        ProductResponse response = productMapper.toResponse(updatedProduct);
        return response;
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