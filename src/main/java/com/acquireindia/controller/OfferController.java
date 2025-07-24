package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Listing;
import com.acquireindia.model.Offer;
import com.acquireindia.model.User;
import com.acquireindia.service.EmailService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/offers/{id}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Offer>> getOfferById(@PathVariable Long id,
                                                           Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Offer offer = offerService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Offer not found"));

            // Check if user has permission to view this offer
            if (!offer.getBuyer().getId().equals(user.getId()) &&
                    !offer.getListing().getSeller().getId().equals(user.getId()) &&
                    !user.getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You don't have permission to view this offer"));
            }

            return ResponseEntity.ok(ApiResponse.success("Offer retrieved successfully", offer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve offer: " + e.getMessage()));
        }
    }
}