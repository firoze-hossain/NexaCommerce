package com.roze.nexacommerce.hotdeal.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealRequest;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealUpdateRequest;
import com.roze.nexacommerce.hotdeal.dto.response.HotDealResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotDealService {
    HotDealResponse createHotDeal(HotDealRequest request);
    HotDealResponse getHotDealById(Long id);
    PaginatedResponse<HotDealResponse> getAllHotDeals(Pageable pageable);
    List<HotDealResponse> getActiveHotDeals();
    HotDealResponse updateHotDeal(Long id, HotDealUpdateRequest request);
    HotDealResponse updateHotDealStatus(Long id, Boolean isActive);
    void deleteHotDeal(Long id);
    HotDealResponse incrementSoldCount(Long id);
    boolean isProductInHotDeal(Long productId);
}