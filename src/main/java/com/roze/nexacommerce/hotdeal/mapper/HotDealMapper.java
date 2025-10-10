// HotDealMapper.java
package com.roze.nexacommerce.hotdeal.mapper;

import com.roze.nexacommerce.hotdeal.dto.request.HotDealRequest;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealUpdateRequest;
import com.roze.nexacommerce.hotdeal.dto.response.HotDealResponse;
import com.roze.nexacommerce.hotdeal.entity.HotDeal;
import com.roze.nexacommerce.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotDealMapper {

    private final ModelMapper modelMapper;
    private final ProductMapper productMapper;

    public HotDeal toEntity(HotDealRequest request) {
        return modelMapper.map(request, HotDeal.class);
    }

    public HotDealResponse toResponse(HotDeal hotDeal) {
        if (hotDeal == null) {
            return null;
        }

        HotDealResponse response = modelMapper.map(hotDeal, HotDealResponse.class);

        // Map product
        if (hotDeal.getProduct() != null) {
            response.setProduct(productMapper.toResponse(hotDeal.getProduct()));
        }

        // Set computed fields
        response.setIsCurrentlyActive(hotDeal.isCurrentlyActive());
        response.setRemainingStock(hotDeal.getRemainingStock());
        response.setDiscountPercentage(hotDeal.getDiscountPercentage());

        return response;
    }

    public void updateEntity(HotDealUpdateRequest request, HotDeal hotDeal) {
        modelMapper.map(request, hotDeal);
    }
}