package com.acquireindia.repository;

import com.acquireindia.model.Listing;
import com.acquireindia.model.ListingView;
import com.acquireindia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingViewRepository extends JpaRepository<ListingView, Long> {
    
    // Count total views for a listing
    Long countByListing(Listing listing);
    
    // Count views for a seller's listings
    @Query("SELECT COUNT(lv) FROM ListingView lv WHERE lv.listing.seller = :seller")
    Long countByListingSeller(@Param("seller") User seller);
    
    // Count views for a listing within a date range
    @Query("SELECT COUNT(lv) FROM ListingView lv WHERE lv.listing = :listing AND lv.viewedAt >= :startDate AND lv.viewedAt <= :endDate")
    Long countByListingAndViewedAtBetween(@Param("listing") Listing listing, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Get daily view counts for a listing (last 30 days)
    @Query("SELECT DATE(lv.viewedAt) as date, COUNT(lv) as count FROM ListingView lv " +
           "WHERE lv.listing = :listing AND lv.viewedAt >= :startDate " +
           "GROUP BY DATE(lv.viewedAt) ORDER BY date")
    List<Object[]> getDailyViewCounts(@Param("listing") Listing listing, 
                                     @Param("startDate") LocalDateTime startDate);
    
    // Get unique viewers count for a listing
    @Query("SELECT COUNT(DISTINCT lv.viewer) FROM ListingView lv WHERE lv.listing = :listing AND lv.viewer IS NOT NULL")
    Long countUniqueViewers(@Param("listing") Listing listing);
    
    // Check if a user has already viewed a listing (for duplicate prevention)
    @Query("SELECT COUNT(lv) > 0 FROM ListingView lv WHERE lv.listing = :listing AND lv.viewer = :viewer")
    boolean existsByListingAndViewer(@Param("listing") Listing listing, @Param("viewer") User viewer);
    
    // Check if an IP has already viewed a listing recently (for anonymous views)
    @Query("SELECT COUNT(lv) > 0 FROM ListingView lv WHERE lv.listing = :listing AND lv.ipAddress = :ipAddress AND lv.viewedAt >= :recentTime")
    boolean existsByListingAndIpAddressAndViewedAtAfter(@Param("listing") Listing listing, 
                                                       @Param("ipAddress") String ipAddress, 
                                                       @Param("recentTime") LocalDateTime recentTime);
    
    // Get recent views for a listing
    List<ListingView> findByListingOrderByViewedAtDesc(Listing listing);
    
    // Get views by seller
    @Query("SELECT lv FROM ListingView lv WHERE lv.listing.seller = :seller ORDER BY lv.viewedAt DESC")
    List<ListingView> findByListingSeller(@Param("seller") User seller);
} 