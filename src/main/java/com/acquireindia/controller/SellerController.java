package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Offer;
import com.acquireindia.model.User;
import com.acquireindia.service.ChatService;
import com.acquireindia.service.ListingService;
import com.acquireindia.service.OfferService;
import com.acquireindia.service.UserService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
public class SellerController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOffersForMyListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long listingId,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
            Page<Offer> offersPage = offerService.findByListingSeller(seller, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("offers", offersPage.getContent());
            response.put("totalCount", offersPage.getTotalElements());

            return ResponseEntity.ok(ApiResponse.success("Offers retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve offers: " + e.getMessage()));
        }
    }

    @GetMapping("/listings/{listingId}/offers")
    public ResponseEntity<ApiResponse<List<Offer>>> getOffersForListing(@PathVariable Long listingId,
                                                                        Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            com.acquireindia.model.Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns the listing
            if (!listing.getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to view these offers"));
            }

            List<Offer> offers = offerService.findByListing(listing);
            return ResponseEntity.ok(ApiResponse.success("Offers retrieved successfully", offers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve offers: " + e.getMessage()));
        }
    }

    @PutMapping("/offers/{id}/status")
    public ResponseEntity<ApiResponse<Offer>> updateOfferStatus(@PathVariable Long id,
                                                                @RequestParam String status,
                                                                Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Offer offer = offerService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));

            // Check if user owns the listing
            if (!offer.getListing().getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to update this offer"));
            }

            Offer.OfferStatus offerStatus = Offer.OfferStatus.valueOf(status.toUpperCase());
            Offer updatedOffer = offerService.updateOfferStatus(id, offerStatus);

            return ResponseEntity.ok(ApiResponse.success("Offer status updated successfully", updatedOffer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update offer status: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> dashboard = new HashMap<>();

            // Stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalListings", listingService.countBySeller(seller));
            stats.put("activeListings", listingService.findBySeller(seller).size());
            stats.put("totalViews", 0); // TODO: Implement view tracking
            stats.put("totalOffers", offerService.countBySeller(seller));
            dashboard.put("stats", stats);

            // Recent offers
            Pageable recentOffersPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<Offer> recentOffersPage = offerService.findByListingSeller(seller, recentOffersPageable);
            dashboard.put("recentOffers", recentOffersPage.getContent());

            // Listing performance (simplified)
            dashboard.put("listingPerformance", listingService.findBySeller(seller));

            return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard data: " + e.getMessage()));
        }
    }

    @GetMapping("/listings/{listingId}/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getListingAnalytics(@PathVariable Long listingId,
                                                                                Authentication authentication) {
        try {
            User seller = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            com.acquireindia.model.Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if user owns the listing
            if (!listing.getSeller().getId().equals(seller.getId()) &&
                    !seller.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to view analytics for this listing"));
            }

            Map<String, Object> analytics = new HashMap<>();

            // Views (placeholder - implement view tracking)
            Map<String, Object> views = new HashMap<>();
            views.put("total", 0);
            views.put("daily", List.of());
            analytics.put("views", views);

            // Offers
            List<Offer> offers = offerService.findByListing(listing);
            Map<String, Object> offersData = new HashMap<>();
            offersData.put("total", offers.size());

            Map<String, Long> offersByStatus = new HashMap<>();
            offersByStatus.put("pending", offers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.PENDING).count());
            offersByStatus.put("accepted", offers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.ACCEPTED).count());
            offersByStatus.put("rejected", offers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.REJECTED).count());
            offersData.put("byStatus", offersByStatus);
            analytics.put("offers", offersData);

            // Engagement (placeholder)
            Map<String, Object> engagement = new HashMap<>();
            engagement.put("saves", 0);
            engagement.put("messages", 0);
            analytics.put("engagement", engagement);

            return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve analytics: " + e.getMessage()));
        }
    }
}