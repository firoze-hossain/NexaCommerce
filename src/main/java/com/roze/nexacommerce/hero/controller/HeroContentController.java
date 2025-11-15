
package com.roze.nexacommerce.hero.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.hero.dto.request.HeroContentRequest;
import com.roze.nexacommerce.hero.dto.response.HeroAnalyticsResponse;
import com.roze.nexacommerce.hero.dto.response.HeroContentResponse;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.service.HeroContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hero")
@RequiredArgsConstructor
@Tag(name = "Hero Content", description = "APIs for managing hero section content")
public class HeroContentController extends BaseController {

    private final HeroContentService heroContentService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CONTENT')")
    @Operation(summary = "Create new hero content")
    public ResponseEntity<BaseResponse<HeroContentResponse>> createHeroContent(
            @Valid @RequestBody HeroContentRequest request) {
        HeroContentResponse response = heroContentService.createHeroContent(request);
        return created(response, "Hero content created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @Operation(summary = "Get hero content by ID")
    public ResponseEntity<BaseResponse<HeroContentResponse>> getHeroContentById(@PathVariable Long id) {
        HeroContentResponse response = heroContentService.getHeroContentById(id);
        return ok(response, "Hero content retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @Operation(summary = "Get all hero content")
    public ResponseEntity<BaseResponse<List<HeroContentResponse>>> getAllHeroContent() {
        List<HeroContentResponse> response = heroContentService.getAllHeroContent();
        return ok(response, "Hero content list retrieved successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_CONTENT')")
    @Operation(summary = "Update hero content")
    public ResponseEntity<BaseResponse<HeroContentResponse>> updateHeroContent(
            @PathVariable Long id,
            @Valid @RequestBody HeroContentRequest request) {
        HeroContentResponse response = heroContentService.updateHeroContent(id, request);
        return ok(response, "Hero content updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_CONTENT')")
    @Operation(summary = "Delete hero content")
    public ResponseEntity<BaseResponse<Void>> deleteHeroContent(@PathVariable Long id) {
        heroContentService.deleteHeroContent(id);
        return noContent("Hero content deleted successfully");
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('UPDATE_CONTENT')")
    @Operation(summary = "Toggle hero content status")
    public ResponseEntity<BaseResponse<HeroContentResponse>> toggleHeroContentStatus(@PathVariable Long id) {
        HeroContentResponse response = heroContentService.toggleHeroContentStatus(id);
        return ok(response, "Hero content status updated successfully");
    }

    // Public APIs
    @GetMapping("/public/active")
    @Operation(summary = "Get active hero content for current user")
    public ResponseEntity<BaseResponse<List<HeroContentResponse>>> getActiveHeroContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String segment,
            HttpServletRequest request) {

        String userSegment = segment != null ? segment : extractUserSegment(request);
        List<HeroContentResponse> response = heroContentService.getActiveHeroContent(
                userDetails != null ? (com.roze.nexacommerce.user.entity.User) userDetails : null,
                userSegment
        );
        return ok(response, "Active hero content retrieved successfully");
    }

    @GetMapping("/public/main")
    @Operation(summary = "Get main hero banner")
    public ResponseEntity<BaseResponse<HeroContentResponse>> getMainHeroContent(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String userSegment = extractUserSegment(request);
        HeroContentResponse response = heroContentService.getMainHeroContent(
                userDetails != null ? (com.roze.nexacommerce.user.entity.User) userDetails : null,
                userSegment
        );

        if (response != null) {
            return ok(response, "Main hero content retrieved successfully");
        } else {
            return ok(null, "No active hero content found");
        }
    }

    @GetMapping("/public/type/{type}")
    @Operation(summary = "Get hero content by type")
    public ResponseEntity<BaseResponse<List<HeroContentResponse>>> getHeroContentByType(
            @PathVariable HeroType type,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String segment,
            HttpServletRequest request) {

        String userSegment = segment != null ? segment : extractUserSegment(request);
        List<HeroContentResponse> response = heroContentService.getHeroContentByType(
                type,
                userDetails != null ? (com.roze.nexacommerce.user.entity.User) userDetails : null,
                userSegment
        );
        return ok(response, "Hero content by type retrieved successfully");
    }

    // Analytics APIs
    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @Operation(summary = "Get hero content analytics")
    public ResponseEntity<BaseResponse<List<HeroAnalyticsResponse>>> getHeroAnalytics() {
        List<HeroAnalyticsResponse> response = heroContentService.getHeroAnalytics();
        return ok(response, "Hero analytics retrieved successfully");
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @Operation(summary = "Get analytics for specific hero content")
    public ResponseEntity<BaseResponse<HeroAnalyticsResponse>> getHeroContentAnalytics(@PathVariable Long id) {
        HeroAnalyticsResponse response = heroContentService.getHeroContentAnalytics(id);
        return ok(response, "Hero content analytics retrieved successfully");
    }

    @PostMapping("/public/{id}/impression")
    @Operation(summary = "Record hero content impression")
    public ResponseEntity<BaseResponse<Void>> recordImpression(@PathVariable Long id) {
        heroContentService.recordImpression(id);
        return ok(null, "Impression recorded successfully");
    }

    @PostMapping("/public/{id}/click")
    @Operation(summary = "Record hero content click")
    public ResponseEntity<BaseResponse<Void>> recordClick(@PathVariable Long id) {
        heroContentService.recordClick(id);
        return ok(null, "Click recorded successfully");
    }

    // Maintenance APIs
    @PostMapping("/maintenance/deactivate-expired")
    @PreAuthorize("hasAuthority('UPDATE_CONTENT')")
    @Operation(summary = "Manually deactivate expired content")
    public ResponseEntity<BaseResponse<Void>> deactivateExpiredContent() {
        heroContentService.deactivateExpiredContent();
        return ok(null, "Expired content deactivated successfully");
    }

    @GetMapping("/top-performing")
    @PreAuthorize("hasAuthority('READ_CONTENT')")
    @Operation(summary = "Get top performing hero content")
    public ResponseEntity<BaseResponse<List<HeroContentResponse>>> getTopPerformingContent() {
        List<HeroContentResponse> response = heroContentService.getTopPerformingContent();
        return ok(response, "Top performing content retrieved successfully");
    }

    private String extractUserSegment(HttpServletRequest request) {
        // Extract user segment based on various factors
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");

        if (userAgent != null && userAgent.toLowerCase().contains("mobile")) {
            return "MOBILE_USER";
        }

        return "DESKTOP_USER";
    }
}