package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import com.acquireindia.service.ListingService;
import com.acquireindia.service.ListingViewService;
import com.acquireindia.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ListingViewService listingViewService;

    // Public endpoints - no authentication required
    @GetMapping("/public/listings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword) {

        try {
            Sort sort = getSortFromString(sortBy);
            Pageable pageable = PageRequest.of(page, limit, sort);

            Page<Listing> listingsPage;
            if (category != null || minPrice != null || maxPrice != null || location != null || keyword != null) {
                listingsPage = listingService.findListingsWithFilters(category, minPrice, maxPrice, location, keyword, pageable);
            } else {
                listingsPage = listingService.findActiveListings(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("listings", listingsPage.getContent());
            response.put("totalCount", listingsPage.getTotalElements());
            response.put("currentPage", listingsPage.getNumber());
            response.put("totalPages", listingsPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve listings: " + e.getMessage()));
        }
    }

    @GetMapping("/public/listings/{id}")
    public ResponseEntity<ApiResponse<Listing>> getPublicListingById(@PathVariable Long id,
                                                                   HttpServletRequest request,
                                                                   Authentication authentication) {
        try {
            Listing listing = listingService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Track the view
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

            return ResponseEntity.ok(ApiResponse.success("Listing retrieved successfully", listing));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve listing: " + e.getMessage()));
        }
    }

    @GetMapping("/public/categories")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getCategories() {
        try {
            List<String> categories = listingService.getDistinctCategories();
            Map<String, List<String>> response = new HashMap<>();
            response.put("categories", categories);

            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }

    @GetMapping("/public/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<Listing> activeListings = listingService.findByStatus(Listing.ListingStatus.ACTIVE);
            stats.put("totalListings", activeListings.size());

            List<Listing> soldListings = listingService.findByStatus(Listing.ListingStatus.SOLD);
            stats.put("successfulDeals", soldListings.size());

            long verifiedUsers = userService.findByKycStatus(User.KycStatus.APPROVED).size();
            stats.put("verifiedUsers", verifiedUsers);

            // Calculate total value from active listings
            BigDecimal totalValue = activeListings.stream()
                    .map(Listing::getAskingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalValue", totalValue);

            return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve stats: " + e.getMessage()));
        }
    }

    @PostMapping("/seller/listings")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Listing>> createListing(@Valid @RequestBody Listing listing,
                                                              Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            listing.setSeller(seller);
            Listing savedListing = listingService.createListing(listing);

            return ResponseEntity.ok(ApiResponse.success("Listing created successfully", savedListing));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create listing: " + e.getMessage()));
        }
    }

    @GetMapping("/seller/listings")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Listing>>> getMyListings(Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Listing> listings = listingService.findBySeller(seller);
            return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", listings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve listings: " + e.getMessage()));
        }
    }

    @PutMapping("/seller/listings/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Listing>> updateListing(@PathVariable Long id,
                                                              @Valid @RequestBody Listing listing,
                                                              Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing existingListing = listingService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns the listing
            if (!existingListing.getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to update this listing"));
            }

            listing.setId(id);
            listing.setSeller(existingListing.getSeller());
            Listing updatedListing = listingService.updateListing(listing);

            return ResponseEntity.ok(ApiResponse.success("Listing updated successfully", updatedListing));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update listing: " + e.getMessage()));
        }
    }

    @DeleteMapping("/seller/listings/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteListing(@PathVariable Long id,
                                                             Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing listing = listingService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns the listing
            if (!listing.getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to delete this listing"));
            }

            listingService.deleteListing(id);
            return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete listing: " + e.getMessage()));
        }
    }

    private Sort getSortFromString(String sortBy) {
        return switch (sortBy) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "price-low" -> Sort.by("askingPrice").ascending();
            case "price-high" -> Sort.by("askingPrice").descending();
            case "revenue-high" -> Sort.by("revenue").descending();
            case "revenue-low" -> Sort.by("revenue").ascending();
            default -> Sort.by("createdAt").descending(); // newest
        };
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