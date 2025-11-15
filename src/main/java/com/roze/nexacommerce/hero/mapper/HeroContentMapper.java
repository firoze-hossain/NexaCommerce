package com.roze.nexacommerce.hero.mapper;

import com.roze.nexacommerce.hero.dto.request.HeroContentRequest;
import com.roze.nexacommerce.hero.dto.response.HeroContentResponse;
import com.roze.nexacommerce.hero.entity.HeroContent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeroContentMapper {
    private final ModelMapper modelMapper;

    public HeroContent toEntity(HeroContentRequest request) {
        HeroContent heroContent = modelMapper.map(request, HeroContent.class);
        heroContent.setImpressions(0);
        heroContent.setClicks(0);
        heroContent.setConversionRate(0.0);
        return heroContent;
    }

    public HeroContentResponse toResponse(HeroContent heroContent) {
        HeroContentResponse response = modelMapper.map(heroContent, HeroContentResponse.class);
        response.setCurrentlyActive(heroContent.isCurrentlyActive());
        return response;
    }

    public void updateEntityFromRequest(HeroContentRequest request, HeroContent heroContent) {
        modelMapper.map(request, heroContent);
    }
}