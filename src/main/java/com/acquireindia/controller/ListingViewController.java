package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import com.acquireindia.service.ListingService;
import com.acquireindia.service.ListingViewService;
import com.acquireindia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingViewController {

    @Autowired
    private ListingViewService listingViewService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    /**
     * Track a view for a listing
     */
    @PostMapping("/{listingId}/view")
    public ResponseEntity<ApiResponse<String>> trackView(@PathVariable Long listingId,
                                                       HttpServletRequest request,
                                                       Authentication authentication) {
        try {
            Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Get current user if authenticated
            User viewer = null;
            if (authentication != null && authentication.isAuthenticated()) {
                viewer = userService.findByEmail(authentication.getName()).orElse(null);
            }

            if (viewer != null) {
                listingViewService.trackView(listing, viewer, ipAddress, userAgent);
            } else {
                String sessionId = request.getSession().getId();
                listingViewService.trackAnonymousView(listing, ipAddress, userAgent, sessionId);
            }

            return ResponseEntity.ok(ApiResponse.success("View tracked successfully", "View recorded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to track view: " + e.getMessage()));
        }
    }

    /**
     * Get view analytics for a listing (seller only)
     */
    @GetMapping("/{listingId}/analytics/views")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getViewAnalytics(@PathVariable Long listingId,
                                                                           Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns the listing or is admin
            if (!listing.getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to view analytics for this listing"));
            }

            Map<String, Object> analytics = listingViewService.getViewAnalytics(listing);
            return ResponseEntity.ok(ApiResponse.success("View analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve view analytics: " + e.getMessage()));
        }
    }

    /**
     * Get total view count for a listing (public)
     */
    @GetMapping("/{listingId}/views/count")
    public ResponseEntity<ApiResponse<Long>> getViewCount(@PathVariable Long listingId) {
        try {
            Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            Long viewCount = listingViewService.getTotalViews(listing);
            return ResponseEntity.ok(ApiResponse.success("View count retrieved successfully", viewCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve view count: " + e.getMessage()));
        }
    }

    /**
     * Helper method to get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 