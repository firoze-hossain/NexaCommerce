package com.roze.nexacommerce.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long customerId;
    private String customerName;
    private String customerProfileImage;
    private Integer rating;
    private String comment;
    private String title;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Current user's vote status
    private Boolean userVotedHelpful;
    private Boolean userVotedNotHelpful;
}