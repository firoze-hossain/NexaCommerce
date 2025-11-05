package com.roze.nexacommerce.email.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.email.dto.response.EmailTrackingResponse;
import com.roze.nexacommerce.email.service.EmailTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/emails/trackings")
@RequiredArgsConstructor
public class EmailTrackingController extends BaseController {

    private final EmailTrackingService emailTrackingService;
    // 1x1 transparent GIF pixel
    private static final byte[] TRANSPARENT_GIF = new byte[]{
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00,
            (byte) 0x80, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x01, 0x00, 0x00, 0x02, 0x02, 0x44, 0x01, 0x00, 0x3b
    };

    @GetMapping("/track-open/{trackingToken}")
    public ResponseEntity<byte[]> trackEmailOpen(
            @PathVariable String trackingToken,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        log.debug("Tracking email open for token: {}", trackingToken);

        try {
            emailTrackingService.trackEmailOpen(trackingToken, ipAddress, userAgent);

            // Return a 1x1 transparent pixel as GIF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_GIF);
            headers.setContentLength(TRANSPARENT_GIF.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0L);

            return new ResponseEntity<>(TRANSPARENT_GIF, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error tracking email open for token: {}", trackingToken, e);
            // Still return the pixel even if tracking fails
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_GIF);
            headers.setContentLength(TRANSPARENT_GIF.length);
            return new ResponseEntity<>(TRANSPARENT_GIF, headers, HttpStatus.OK);
        }
    }

    @GetMapping("/track-click/{trackingToken}")
    public ResponseEntity<Void> trackEmailClick(
            @PathVariable String trackingToken,
            @RequestParam String url,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        log.debug("Tracking email click for token: {}, URL: {}", trackingToken, url);

        try {
            emailTrackingService.trackEmailClick(trackingToken, url, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Error tracking email click for token: {}", trackingToken, e);
            // Continue with redirect even if tracking fails
        }

        // Redirect to the actual URL
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(url));
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/{trackingToken}")
    public ResponseEntity<BaseResponse<EmailTrackingResponse>> getTrackingByToken(
            @PathVariable String trackingToken) {

        EmailTrackingResponse response = emailTrackingService.getTrackingByToken(trackingToken);
        return ok(response, "Tracking information retrieved successfully");
    }

    @GetMapping("/email-log/{emailLogId}")
    public ResponseEntity<BaseResponse<List<EmailTrackingResponse>>> getTrackingByEmailLog(
            @PathVariable Long emailLogId) {

        List<EmailTrackingResponse> responses = emailTrackingService.getTrackingByEmailLog(emailLogId);
        return ok(responses, "Tracking information retrieved successfully");
    }
}