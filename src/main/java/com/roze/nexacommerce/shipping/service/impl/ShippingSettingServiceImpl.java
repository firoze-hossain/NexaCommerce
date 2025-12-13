package com.roze.nexacommerce.shipping.service.impl;

import com.roze.nexacommerce.shipping.dto.request.ShippingSettingRequest;
import com.roze.nexacommerce.shipping.dto.response.ShippingSettingResponse;
import com.roze.nexacommerce.shipping.entity.ShippingSetting;
import com.roze.nexacommerce.shipping.mapper.ShippingSettingMapper;
import com.roze.nexacommerce.shipping.repository.ShippingSettingRepository;
import com.roze.nexacommerce.shipping.service.ShippingSettingService;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
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
public class ShippingSettingServiceImpl implements ShippingSettingService {
    
    private final ShippingSettingRepository shippingSettingRepository;
    private final ShippingSettingMapper shippingMapper;
    
    @Override
    @Transactional(readOnly = true)
    public ShippingSettingResponse calculateShippingCost(String locationType, BigDecimal orderTotal) {
        ShippingSetting setting = shippingSettingRepository
                .findByLocationTypeAndActiveTrue(locationType)
                .orElseGet(() -> getDefaultShippingSetting(locationType));
        
        return shippingMapper.toResponseWithFreeShipping(setting, orderTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingSettingResponse getShippingSettingById(Long id) {
        ShippingSetting setting = shippingSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingSetting", "id", id));
        return shippingMapper.toResponse(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingSettingResponse getShippingSettingByLocationType(String locationType) {
        ShippingSetting setting = shippingSettingRepository.findByLocationType(locationType)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingSetting", "locationType", locationType));
        return shippingMapper.toResponse(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingSettingResponse> getAllShippingSettings() {
        return shippingSettingRepository.findAll().stream()
                .map(shippingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShippingSettingResponse> getShippingSettings(Pageable pageable) {
        Page<ShippingSetting> shippingPage = shippingSettingRepository.findAll(pageable);
        List<ShippingSettingResponse> shippingResponses = shippingPage.getContent().stream()
                .map(shippingMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<ShippingSettingResponse>builder()
                .items(shippingResponses)
                .totalItems(shippingPage.getTotalElements())
                .currentPage(shippingPage.getNumber())
                .pageSize(shippingPage.getSize())
                .totalPages(shippingPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingSettingResponse> getActiveShippingSettings() {
        return shippingSettingRepository.findByActiveTrue().stream()
                .map(shippingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingSettingResponse createShippingSetting(ShippingSettingRequest request) {
        // Validate unique constraint
        if (shippingSettingRepository.existsByLocationType(request.getLocationType().toUpperCase())) {
            throw new DuplicateResourceException("ShippingSetting", "locationType", request.getLocationType());
        }
        
        // Validate location type
        validateLocationType(request.getLocationType());
        
        ShippingSetting setting = shippingMapper.toEntity(request);
        ShippingSetting savedSetting = shippingSettingRepository.save(setting);
        
        return shippingMapper.toResponse(savedSetting);
    }

    @Override
    @Transactional
    public ShippingSettingResponse updateShippingSetting(Long id, ShippingSettingRequest request) {
        ShippingSetting setting = shippingSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingSetting", "id", id));

        // Validate unique constraint if location type is being changed
        if (request.getLocationType() != null && 
            !request.getLocationType().equalsIgnoreCase(setting.getLocationType()) &&
            shippingSettingRepository.existsByLocationType(request.getLocationType().toUpperCase())) {
            throw new DuplicateResourceException("ShippingSetting", "locationType", request.getLocationType());
        }
        
        // Validate location type if provided
        if (request.getLocationType() != null) {
            validateLocationType(request.getLocationType());
        }

        shippingMapper.updateEntity(request, setting);
        ShippingSetting updatedSetting = shippingSettingRepository.save(setting);
        
        return shippingMapper.toResponse(updatedSetting);
    }

    @Override
    @Transactional
    public void deleteShippingSetting(Long id) {
        ShippingSetting setting = shippingSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingSetting", "id", id));
        
        shippingSettingRepository.delete(setting);
    }

    @Override
    @Transactional
    public ShippingSettingResponse toggleShippingSettingStatus(Long id) {
        ShippingSetting setting = shippingSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingSetting", "id", id));
        
        setting.setActive(!setting.isActive());
        ShippingSetting updatedSetting = shippingSettingRepository.save(setting);
        
        return shippingMapper.toResponse(updatedSetting);
    }

    @Override
    public List<String> getAvailableLocationTypes() {
        return List.of("INSIDE_DHAKA", "OUTSIDE_DHAKA", "EMERGENCY", "EXPRESS");
    }

    private void validateLocationType(String locationType) {
        if (!getAvailableLocationTypes().contains(locationType.toUpperCase())) {
            throw new IllegalArgumentException("Invalid location type. Valid types: " + getAvailableLocationTypes());
        }
    }
    
    private ShippingSetting getDefaultShippingSetting(String locationType) {
        BigDecimal shippingCost = locationType.equalsIgnoreCase("INSIDE_DHAKA") 
            ? new BigDecimal("60.00") 
            : new BigDecimal("120.00");
        
        String deliveryTime = locationType.equalsIgnoreCase("INSIDE_DHAKA") 
            ? "1-2 business days" 
            : "3-5 business days";
        
        return ShippingSetting.builder()
            .locationType(locationType.toUpperCase())
            .shippingCost(shippingCost)
            .deliveryTime(deliveryTime)
            .minimumOrderForFreeShipping(new BigDecimal("1000.00"))
            .active(true)
            .description("Default shipping setting")
            .build();
    }
}