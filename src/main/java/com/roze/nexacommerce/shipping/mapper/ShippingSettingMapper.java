package com.roze.nexacommerce.shipping.mapper;

import com.roze.nexacommerce.shipping.dto.request.ShippingSettingRequest;
import com.roze.nexacommerce.shipping.dto.response.ShippingSettingResponse;
import com.roze.nexacommerce.shipping.entity.ShippingSetting;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ShippingSettingMapper {
    private final ModelMapper modelMapper;

    public ShippingSetting toEntity(ShippingSettingRequest request) {
        ShippingSetting setting = modelMapper.map(request, ShippingSetting.class);
        setting.setLocationType(request.getLocationType().toUpperCase());
        return setting;
    }

    public ShippingSettingResponse toResponse(ShippingSetting setting) {
        return modelMapper.map(setting, ShippingSettingResponse.class);
    }

    public ShippingSettingResponse toResponseWithFreeShipping(ShippingSetting setting, BigDecimal orderTotal) {
        ShippingSettingResponse response = modelMapper.map(setting, ShippingSettingResponse.class);
        
        boolean isFreeShippingEligible = orderTotal.compareTo(setting.getMinimumOrderForFreeShipping()) >= 0;
        BigDecimal freeShippingRemaining = setting.getMinimumOrderForFreeShipping()
                .subtract(orderTotal).max(BigDecimal.ZERO);
        
        response.setShippingCost(isFreeShippingEligible ? BigDecimal.ZERO : setting.getShippingCost());
        response.setFreeShippingEligible(isFreeShippingEligible);
        response.setFreeShippingRemaining(freeShippingRemaining);
        
        return response;
    }

    public void updateEntity(ShippingSettingRequest request, ShippingSetting setting) {
        modelMapper.map(request, setting);
        if (request.getLocationType() != null) {
            setting.setLocationType(request.getLocationType().toUpperCase());
        }
    }
}