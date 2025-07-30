package com.acquireindia.service;

import com.acquireindia.model.Listing;
import com.acquireindia.model.ListingView;
import com.acquireindia.model.User;
import com.acquireindia.repository.ListingViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListingViewService {

    @Autowired
    private ListingViewRepository listingViewRepository;

    /**
     * Track a view for a listing
     */
    public void trackView(Listing listing, User viewer, String ipAddress, String userAgent) {
        // Check if this user has already viewed this listing recently (within 24 hours)
        if (viewer != null && listingViewRepository.existsByListingAndViewer(listing, viewer)) {
            return; // Already viewed by this user
        }

        // For anonymous users, check if IP has viewed recently (within 1 hour)
        if (viewer == null) {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            if (listingViewRepository.existsByListingAndIpAddressAndViewedAtAfter(listing, ipAddress, oneHourAgo)) {
                return; // Already viewed from this IP recently
            }
        }

        // Create and save the view
        ListingView view = new ListingView(listing, viewer, ipAddress, userAgent);
        listingViewRepository.save(view);
    }

    /**
     * Track an anonymous view
     */
    public void trackAnonymousView(Listing listing, String ipAddress, String userAgent, String sessionId) {
        // Check if IP has viewed recently (within 1 hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        if (listingViewRepository.existsByListingAndIpAddressAndViewedAtAfter(listing, ipAddress, oneHourAgo)) {
            return; // Already viewed from this IP recently
        }

        // Create and save the view
        ListingView view = new ListingView(listing, ipAddress, userAgent, sessionId);
        listingViewRepository.save(view);
    }

    /**
     * Get total view count for a listing
     */
    public Long getTotalViews(Listing listing) {
        return listingViewRepository.countByListing(listing);
    }

    /**
     * Get total view count for a seller's listings
     */
    public Long getTotalViewsForSeller(User seller) {
        return listingViewRepository.countByListingSeller(seller);
    }

    /**
     * Get unique viewers count for a listing
     */
    public Long getUniqueViewers(Listing listing) {
        return listingViewRepository.countUniqueViewers(listing);
    }

    /**
     * Get daily view counts for a listing (last 30 days)
     */
    public List<Object[]> getDailyViewCounts(Listing listing) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return listingViewRepository.getDailyViewCounts(listing, thirtyDaysAgo);
    }

    /**
     * Get view analytics for a listing
     */
    public Map<String, Object> getViewAnalytics(Listing listing) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total views
        Long totalViews = getTotalViews(listing);
        analytics.put("totalViews", totalViews);
        
        // Unique viewers
        Long uniqueViewers = getUniqueViewers(listing);
        analytics.put("uniqueViewers", uniqueViewers);
        
        // Daily view counts (last 30 days)
        List<Object[]> dailyViews = getDailyViewCounts(listing);
        analytics.put("dailyViews", dailyViews);
        
        // Recent views (last 10)
        List<ListingView> recentViews = listingViewRepository.findByListingOrderByViewedAtDesc(listing);
        analytics.put("recentViews", recentViews.subList(0, Math.min(10, recentViews.size())));
        
        return analytics;
    }

    /**
     * Get view analytics for a seller
     */
    public Map<String, Object> getSellerViewAnalytics(User seller) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total views across all listings
        Long totalViews = getTotalViewsForSeller(seller);
        analytics.put("totalViews", totalViews);
        
        // Recent views across all listings
        List<ListingView> recentViews = listingViewRepository.findByListingSeller(seller);
        analytics.put("recentViews", recentViews.subList(0, Math.min(20, recentViews.size())));
        
        return analytics;
    }

    /**
     * Get views for a listing within a date range
     */
    public Long getViewsInDateRange(Listing listing, LocalDateTime startDate, LocalDateTime endDate) {
        return listingViewRepository.countByListingAndViewedAtBetween(listing, startDate, endDate);
    }
} 