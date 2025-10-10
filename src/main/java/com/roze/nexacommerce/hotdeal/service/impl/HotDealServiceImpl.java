// HotDealServiceImpl.java
package com.roze.nexacommerce.hotdeal.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealRequest;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealUpdateRequest;
import com.roze.nexacommerce.hotdeal.dto.response.HotDealResponse;
import com.roze.nexacommerce.hotdeal.entity.HotDeal;
import com.roze.nexacommerce.hotdeal.mapper.HotDealMapper;
import com.roze.nexacommerce.hotdeal.repository.HotDealRepository;
import com.roze.nexacommerce.hotdeal.service.HotDealService;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotDealServiceImpl implements HotDealService {

    private final HotDealRepository hotDealRepository;
    private final ProductRepository productRepository;
    private final HotDealMapper hotDealMapper;

    @Override
    @Transactional
    public HotDealResponse createHotDeal(HotDealRequest request) {
        // Check if product already has an active hot deal
        if (hotDealRepository.existsByProductIdAndIsActiveTrue(request.getProductId())) {
            throw new DuplicateResourceException("Hot Deal", "productId", request.getProductId().toString());
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        HotDeal hotDeal = hotDealMapper.toEntity(request);
        hotDeal.setProduct(product);

        HotDeal savedHotDeal = hotDealRepository.save(hotDeal);
        return hotDealMapper.toResponse(savedHotDeal);
    }

    @Override
    public HotDealResponse getHotDealById(Long id) {
        HotDeal hotDeal = hotDealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hot Deal", "id", id));
        return hotDealMapper.toResponse(hotDeal);
    }

    @Override
    public PaginatedResponse<HotDealResponse> getAllHotDeals(Pageable pageable) {
        Page<HotDeal> hotDealPage = hotDealRepository.findAll(pageable);
        return buildPaginatedResponse(hotDealPage);
    }

    @Override
    public List<HotDealResponse> getActiveHotDeals() {
        List<HotDeal> activeDeals = hotDealRepository.findActiveDeals(LocalDateTime.now());
        return activeDeals.stream()
                .map(hotDealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HotDealResponse updateHotDeal(Long id, HotDealUpdateRequest request) {
        HotDeal hotDeal = hotDealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hot Deal", "id", id));

        hotDealMapper.updateEntity(request, hotDeal);
        HotDeal updatedHotDeal = hotDealRepository.save(hotDeal);

        return hotDealMapper.toResponse(updatedHotDeal);
    }

    @Override
    @Transactional
    public HotDealResponse updateHotDealStatus(Long id, Boolean isActive) {
        HotDeal hotDeal = hotDealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hot Deal", "id", id));

        hotDeal.setIsActive(isActive);
        HotDeal updatedHotDeal = hotDealRepository.save(hotDeal);

        return hotDealMapper.toResponse(updatedHotDeal);
    }

    @Override
    @Transactional
    public void deleteHotDeal(Long id) {
        HotDeal hotDeal = hotDealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hot Deal", "id", id));
        hotDealRepository.delete(hotDeal);
    }

    @Override
    @Transactional
    public HotDealResponse incrementSoldCount(Long id) {
        HotDeal hotDeal = hotDealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hot Deal", "id", id));

        hotDeal.setSoldCount(hotDeal.getSoldCount() + 1);
        HotDeal updatedHotDeal = hotDealRepository.save(hotDeal);

        return hotDealMapper.toResponse(updatedHotDeal);
    }

    @Override
    public boolean isProductInHotDeal(Long productId) {
        return hotDealRepository.existsByProductIdAndIsActiveTrue(productId);
    }

    private PaginatedResponse<HotDealResponse> buildPaginatedResponse(Page<HotDeal> hotDealPage) {
        List<HotDealResponse> responses = hotDealPage.getContent().stream()
                .map(hotDealMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<HotDealResponse>builder()
                .items(responses)
                .totalItems(hotDealPage.getTotalElements())
                .currentPage(hotDealPage.getNumber())
                .pageSize(hotDealPage.getSize())
                .totalPages(hotDealPage.getTotalPages())
                .build();
    }
}