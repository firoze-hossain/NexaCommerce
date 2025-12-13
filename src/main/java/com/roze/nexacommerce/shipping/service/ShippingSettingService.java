package com.roze.nexacommerce.shipping.service;

import com.roze.nexacommerce.shipping.dto.request.ShippingSettingRequest;
import com.roze.nexacommerce.shipping.dto.response.ShippingSettingResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingSettingService {
    ShippingSettingResponse calculateShippingCost(String locationType, BigDecimal orderTotal);
    
    ShippingSettingResponse getShippingSettingById(Long id);
    
    ShippingSettingResponse getShippingSettingByLocationType(String locationType);
    
    List<ShippingSettingResponse> getAllShippingSettings();
    
    PaginatedResponse<ShippingSettingResponse> getShippingSettings(Pageable pageable);
    
    List<ShippingSettingResponse> getActiveShippingSettings();
    
    ShippingSettingResponse createShippingSetting(ShippingSettingRequest request);
    
    ShippingSettingResponse updateShippingSetting(Long id, ShippingSettingRequest request);
    
    void deleteShippingSetting(Long id);
    
    ShippingSettingResponse toggleShippingSettingStatus(Long id);
    
    List<String> getAvailableLocationTypes();
}