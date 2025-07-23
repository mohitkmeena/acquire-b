package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Listing;
import com.acquireindia.model.Offer;
import com.acquireindia.model.SavedListing;
import com.acquireindia.model.User;
import com.acquireindia.service.*;
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
@RequestMapping("/api/buyer")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('BUYER') or hasRole('ADMIN')")
public class BuyerController {

    @Autowired
    private OfferService offerService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    @Autowired
    private SavedListingService savedListingService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/offers")
    public ResponseEntity<ApiResponse<Offer>> createOffer(@Valid @RequestBody Offer offer,
                                                          Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing listing = listingService.findById(offer.getListing().getId())
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            // Check if buyer already has an offer for this listing
            if (offerService.findByBuyerAndListing(buyer, listing).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You already have an offer for this listing"));
            }

            offer.setBuyer(buyer);
            offer.setListing(listing);
            Offer savedOffer = offerService.createOffer(offer);

            // Send email notification to seller
            emailService.sendOfferNotification(
                    listing.getSeller().getEmail(),
                    listing.getSeller().getName(),
                    listing.getTitle(),
                    buyer.getName()
            );

            return ResponseEntity.ok(ApiResponse.success("Offer created successfully", savedOffer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create offer: " + e.getMessage()));
        }
    }

    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
            Page<Offer> offersPage = offerService.findByBuyer(buyer, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("offers", offersPage.getContent());
            response.put("totalCount", offersPage.getTotalElements());

            return ResponseEntity.ok(ApiResponse.success("Offers retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve offers: " + e.getMessage()));
        }
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<Offer>> getOfferById(@PathVariable Long id,
                                                           Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Offer offer = offerService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));

            // Check if user owns this offer
            if (!offer.getBuyer().getId().equals(buyer.getId()) &&
                    !buyer.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to view this offer"));
            }

            return ResponseEntity.ok(ApiResponse.success("Offer retrieved successfully", offer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve offer: " + e.getMessage()));
        }
    }

    @PutMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<Offer>> updateOffer(@PathVariable Long id,
                                                          @Valid @RequestBody Offer offerUpdate,
                                                          Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Offer existingOffer = offerService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));

            // Check if user owns this offer
            if (!existingOffer.getBuyer().getId().equals(buyer.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to update this offer"));
            }

            // Only allow updates if offer is still pending
            if (existingOffer.getStatus() != Offer.OfferStatus.PENDING) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot update offer that is not pending"));
            }

            existingOffer.setAmount(offerUpdate.getAmount());
            existingOffer.setMessage(offerUpdate.getMessage());

            Offer updatedOffer = offerService.updateOffer(existingOffer);
            return ResponseEntity.ok(ApiResponse.success("Offer updated successfully", updatedOffer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update offer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOffer(@PathVariable Long id,
                                                           Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Offer offer = offerService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));

            // Check if user owns this offer
            if (!offer.getBuyer().getId().equals(buyer.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to delete this offer"));
            }

            // Only allow deletion if offer is still pending
            if (offer.getStatus() != Offer.OfferStatus.PENDING) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot delete offer that is not pending"));
            }

            offerService.deleteOffer(id);
            return ResponseEntity.ok(ApiResponse.success("Offer deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete offer: " + e.getMessage()));
        }
    }

    @GetMapping("/saved-listings")
    public ResponseEntity<ApiResponse<Map<String, List<SavedListing>>>> getSavedListings(
            Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<SavedListing> savedListings = savedListingService.findByUser(buyer);

            Map<String, List<SavedListing>> response = new HashMap<>();
            response.put("savedListings", savedListings);

            return ResponseEntity.ok(ApiResponse.success("Saved listings retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve saved listings: " + e.getMessage()));
        }
    }

    @PostMapping("/saved-listings/{listingId}")
    public ResponseEntity<ApiResponse<String>> saveListing(@PathVariable Long listingId,
                                                           @RequestBody(required = false) Map<String, String> requestBody,
                                                           Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            String notes = requestBody != null ? requestBody.get("notes") : null;
            savedListingService.saveListing(buyer, listing, notes);

            return ResponseEntity.ok(ApiResponse.success("Listing saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save listing: " + e.getMessage()));
        }
    }

    @DeleteMapping("/saved-listings/{listingId}")
    public ResponseEntity<ApiResponse<String>> removeSavedListing(@PathVariable Long listingId,
                                                                  Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Listing listing = listingService.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));

            savedListingService.removeSavedListing(buyer, listing);
            return ResponseEntity.ok(ApiResponse.success("Saved listing removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to remove saved listing: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Authentication authentication) {
        try {
            User buyer = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> dashboard = new HashMap<>();

            // Stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOffers", offerService.countByBuyer(buyer));
            stats.put("activeOffers", offerService.findBySellerAndStatus(buyer, Offer.OfferStatus.PENDING).size());
            stats.put("savedListings", savedListingService.countByUser(buyer));
            stats.put("messagesUnread", chatService.getUnreadMessageCount(buyer));
            dashboard.put("stats", stats);

            // Recent offers
            Pageable recentOffersPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<Offer> recentOffersPage = offerService.findByBuyer(buyer, recentOffersPageable);
            dashboard.put("recentOffers", recentOffersPage.getContent());

            // Recommended listings (for now, just get recent active listings)
            Pageable recommendedPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<Listing> recommendedPage = listingService.findActiveListings(recommendedPageable);
            dashboard.put("recommendedListings", recommendedPage.getContent());

            return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard data: " + e.getMessage()));
        }
    }
}