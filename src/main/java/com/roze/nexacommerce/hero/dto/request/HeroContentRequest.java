
package com.roze.nexacommerce.hero.dto.request;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.enums.TargetAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroContentRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String subtitle;
    
    private String description;
    
    @NotBlank(message = "Background image is required")
    private String backgroundImage;
    
    private String overlayColor;
    
    @Builder.Default
    private Double overlayOpacity = 0.8;
    
    @Builder.Default
    private Boolean active = true;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
    
    private String button1Text;
    private String button1Url;
    private String button1Color;
    
    private String button2Text;
    private String button2Url;
    private String button2Color;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Type is required")
    private HeroType type;
    
    @NotNull(message = "Target audience is required")
    private TargetAudience targetAudience;
    
    private String segmentFilters;
}