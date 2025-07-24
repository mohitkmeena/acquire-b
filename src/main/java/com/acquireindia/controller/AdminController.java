package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.Listing;
import com.acquireindia.model.Offer;
import com.acquireindia.model.Transaction;
import com.acquireindia.model.User;
import com.acquireindia.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ListingService listingService;
    
    @Autowired
    private OfferService offerService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            List<User> allUsers = userService.findAll();
            stats.put("totalUsers", allUsers.size());
            stats.put("totalBuyers", allUsers.stream().filter(u -> u.getRole() == User.Role.BUYER).count());
            stats.put("totalSellers", allUsers.stream().filter(u -> u.getRole() == User.Role.SELLER).count());
            
            List<Listing> activeListings = listingService.findByStatus(Listing.ListingStatus.ACTIVE);
            stats.put("totalActiveListings", activeListings.size());
            
            List<Offer> pendingOffers = offerService.findBySellerAndStatus(null, Offer.OfferStatus.PENDING);
            stats.put("totalPendingOffers", pendingOffers.size());
            
            List<User> pendingKyc = userService.findByKycStatus(User.KycStatus.PENDING);
            stats.put("pendingKycVerifications", pendingKyc.size());
            
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve dashboard stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users/kyc-pending")
    public ResponseEntity<ApiResponse<List<User>>> getKycPendingUsers() {
        try {
            List<User> users = userService.findByKycStatus(User.KycStatus.PENDING);
            return ResponseEntity.ok(ApiResponse.success("KYC pending users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve KYC pending users: " + e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}/kyc-status")
    public ResponseEntity<ApiResponse<User>> updateKycStatus(@PathVariable Long id,
                                                             @RequestParam User.KycStatus status) {
        try {
            User user = userService.updateKycStatus(id, status);
            
            // Send email notification
            emailService.sendKycStatusEmail(user.getEmail(), user.getName(), status.toString());
            
            return ResponseEntity.ok(ApiResponse.success("KYC status updated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update KYC status: " + e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(@PathVariable Long id,
                                                              @RequestParam User.UserStatus status) {
        try {
            User user = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update user status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/listings")
    public ResponseEntity<ApiResponse<Page<Listing>>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Listing> listings = listingService.findActiveListings(pageable);
            return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", listings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve listings: " + e.getMessage()));
        }
    }
    
    @PutMapping("/listings/{id}/status")
    public ResponseEntity<ApiResponse<Listing>> updateListingStatus(@PathVariable Long id,
                                                                    @RequestParam Listing.ListingStatus status) {
        try {
            Listing listing = listingService.updateListingStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Listing status updated successfully", listing));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update listing status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactions = transactionService.findAllOrderByCreatedAtDesc(pageable);
            return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve transactions: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<String>> deleteListing(@PathVariable Long id) {
        try {
            listingService.deleteListing(id);
            return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete listing: " + e.getMessage()));
        }
    }
}