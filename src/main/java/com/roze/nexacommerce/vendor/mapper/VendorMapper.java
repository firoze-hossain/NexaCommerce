package com.roze.nexacommerce.vendor.mapper;

import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.vendor.dto.request.VendorRegistrationRequest;
import com.roze.nexacommerce.vendor.dto.request.VendorUpdateRequest;
import com.roze.nexacommerce.vendor.dto.response.UserInfo;
import com.roze.nexacommerce.vendor.dto.response.VendorDetailResponse;
import com.roze.nexacommerce.vendor.dto.response.VendorResponse;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorMapper {
    private final ModelMapper modelMapper;

    public VendorProfile toEntity(VendorRegistrationRequest request, User user) {
        VendorProfile vendor = modelMapper.map(request, VendorProfile.class);
        vendor.setUser(user);
        return vendor;
    }

    public VendorResponse toResponse(VendorProfile vendor) {
        return modelMapper.map(vendor, VendorResponse.class);
    }

    public VendorDetailResponse toDetailResponse(VendorProfile vendor) {
        VendorResponse vendorResponse = toResponse(vendor);
        User user = vendor.getUser();

        UserInfo userInfo = UserInfo.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .active(user.getActive())
                .build();

        return VendorDetailResponse.builder()
                .vendor(vendorResponse)
                .userInfo(userInfo)
                .build();
    }

    public void updateEntity(VendorUpdateRequest request, VendorProfile vendor) {
        modelMapper.map(request, vendor);
    }
}