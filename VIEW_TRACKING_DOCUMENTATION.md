# View Tracking Implementation Documentation

## Overview

This document describes the comprehensive view tracking system implemented for the Acquire India platform. The system tracks views for business listings with support for both authenticated and anonymous users.

## Architecture

### Components

1. **ListingView Entity** - Database model for storing view data
2. **ListingViewRepository** - Data access layer with analytics queries
3. **ListingViewService** - Business logic for view tracking and analytics
4. **ListingViewController** - REST API endpoints for view tracking
5. **Integration with existing controllers** - Automatic view tracking

### Database Schema

```sql
CREATE TABLE listing_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    viewer_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    viewed_at DATETIME NOT NULL,
    anonymous_id VARCHAR(255),
    FOREIGN KEY (listing_id) REFERENCES listings(id),
    FOREIGN KEY (viewer_id) REFERENCES users(id)
);
```

## Features

### 1. View Tracking
- **Authenticated Users**: Track views with user information
- **Anonymous Users**: Track views with IP address and session
- **Duplicate Prevention**: Prevent multiple views from same user/IP within time limits
- **IP Address Detection**: Support for proxy headers (X-Forwarded-For, X-Real-IP)

### 2. Analytics
- **Total Views**: Count of all views for a listing
- **Unique Viewers**: Count of distinct authenticated viewers
- **Daily View Trends**: View counts by day (last 30 days)
- **Seller Analytics**: Combined analytics for all seller's listings

### 3. Security & Privacy
- **IP Address Tracking**: For anonymous users only
- **Session-based Tracking**: For anonymous users
- **Time-based Deduplication**: Prevents spam views
- **User Consent**: Only tracks authenticated users who access listings

## API Endpoints

### 1. Track View (Manual)
```
POST /api/listings/{listingId}/view
```
- **Purpose**: Manually track a view for a listing
- **Authentication**: Optional (supports both authenticated and anonymous)
- **Response**: Success/error message

### 2. Get View Analytics (Seller Only)
```
GET /api/listings/{listingId}/analytics/views
```
- **Purpose**: Get detailed view analytics for a listing
- **Authentication**: Required (seller or admin only)
- **Response**: Total views, unique viewers, daily trends, recent views

### 3. Get View Count (Public)
```
GET /api/listings/{listingId}/views/count
```
- **Purpose**: Get total view count for a listing
- **Authentication**: Not required
- **Response**: Total view count

### 4. Automatic View Tracking
```
GET /api/public/listings/{id}
```
- **Purpose**: Get listing details with automatic view tracking
- **Authentication**: Optional
- **Response**: Listing details (view tracked automatically)

## Implementation Details

### View Tracking Logic

```java
// For authenticated users
if (viewer != null) {
    // Check if user has already viewed this listing
    if (listingViewRepository.existsByListingAndViewer(listing, viewer)) {
        return; // Already viewed
    }
    // Track the view
    ListingView view = new ListingView(listing, viewer, ipAddress, userAgent);
    listingViewRepository.save(view);
}

// For anonymous users
if (viewer == null) {
    // Check if IP has viewed recently (within 1 hour)
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    if (listingViewRepository.existsByListingAndIpAddressAndViewedAtAfter(
        listing, ipAddress, oneHourAgo)) {
        return; // Already viewed from this IP recently
    }
    // Track the view
    ListingView view = new ListingView(listing, ipAddress, userAgent, sessionId);
    listingViewRepository.save(view);
}
```

### Analytics Queries

```java
// Total views for a listing
@Query("SELECT COUNT(lv) FROM ListingView lv WHERE lv.listing = :listing")
Long countByListing(@Param("listing") Listing listing);

// Unique viewers for a listing
@Query("SELECT COUNT(DISTINCT lv.viewer) FROM ListingView lv WHERE lv.listing = :listing AND lv.viewer IS NOT NULL")
Long countUniqueViewers(@Param("listing") Listing listing);

// Daily view counts (last 30 days)
@Query("SELECT DATE(lv.viewedAt) as date, COUNT(lv) as count FROM ListingView lv " +
       "WHERE lv.listing = :listing AND lv.viewedAt >= :startDate " +
       "GROUP BY DATE(lv.viewedAt) ORDER BY date")
List<Object[]> getDailyViewCounts(@Param("listing") Listing listing, 
                                 @Param("startDate") LocalDateTime startDate);
```

## Integration Points

### 1. Seller Dashboard
- **Location**: `SellerController.getDashboard()`
- **Feature**: Shows total views across all seller's listings
- **Implementation**: `listingViewService.getTotalViewsForSeller(seller)`

### 2. Listing Analytics
- **Location**: `SellerController.getListingAnalytics()`
- **Feature**: Shows detailed view analytics for individual listings
- **Implementation**: 
  - `listingViewService.getTotalViews(listing)`
  - `listingViewService.getUniqueViewers(listing)`
  - `listingViewService.getDailyViewCounts(listing)`

### 3. Public Listing View
- **Location**: `ListingController.getPublicListingById()`
- **Feature**: Automatically tracks views when listings are accessed
- **Implementation**: Integrated view tracking in the endpoint

## Configuration

### Time-based Deduplication
- **Authenticated Users**: No duplicate views (one per user per listing)
- **Anonymous Users**: 1-hour cooldown between views from same IP

### Analytics Retention
- **Daily Trends**: Last 30 days
- **Recent Views**: Last 10 views per listing
- **Seller Analytics**: Last 20 views across all listings

## Security Considerations

### 1. Privacy Protection
- **IP Address**: Only stored for anonymous users
- **User Data**: Minimal data collection (IP, User-Agent, timestamp)
- **Session Data**: Temporary session tracking for anonymous users

### 2. Spam Prevention
- **Time-based Limits**: Prevents rapid-fire view tracking
- **IP-based Deduplication**: Prevents multiple views from same IP
- **User-based Deduplication**: Prevents multiple views from same user

### 3. Data Protection
- **GDPR Compliance**: Minimal data collection
- **Data Retention**: Configurable retention policies
- **User Control**: Users can opt-out by not accessing listings

## Performance Considerations

### 1. Database Optimization
- **Indexes**: On listing_id, viewer_id, viewed_at, ip_address
- **Query Optimization**: Efficient analytics queries
- **Batch Operations**: For bulk analytics processing

### 2. Caching Strategy
- **View Counts**: Cache frequently accessed view counts
- **Analytics**: Cache computed analytics for performance
- **Session Data**: In-memory session tracking

### 3. Scalability
- **Horizontal Scaling**: Stateless view tracking
- **Database Sharding**: For high-volume view tracking
- **CDN Integration**: For global view tracking

## Usage Examples

### Frontend Integration

```javascript
// Track view when listing is viewed
async function trackListingView(listingId) {
    try {
        await fetch(`/api/listings/${listingId}/view`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // Optional
            }
        });
    } catch (error) {
        console.error('Failed to track view:', error);
    }
}

// Get view count for display
async function getViewCount(listingId) {
    try {
        const response = await fetch(`/api/listings/${listingId}/views/count`);
        const data = await response.json();
        return data.data; // View count
    } catch (error) {
        console.error('Failed to get view count:', error);
        return 0;
    }
}

// Display view count
function displayViewCount(listingId) {
    getViewCount(listingId).then(count => {
        document.getElementById(`views-${listingId}`).textContent = count;
    });
}
```

### Seller Analytics Dashboard

```javascript
// Get detailed analytics for seller dashboard
async function getListingAnalytics(listingId) {
    try {
        const response = await fetch(`/api/listings/${listingId}/analytics/views`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        
        // Update dashboard with analytics
        updateAnalyticsChart(data.data.dailyViews);
        updateViewCount(data.data.totalViews);
        updateUniqueViewers(data.data.uniqueViewers);
    } catch (error) {
        console.error('Failed to get analytics:', error);
    }
}
```

## Testing

### Unit Tests

```java
@Test
public void testTrackView() {
    // Test authenticated user view tracking
    User viewer = createTestUser();
    Listing listing = createTestListing();
    
    listingViewService.trackView(listing, viewer, "192.168.1.1", "Mozilla/5.0");
    
    Long viewCount = listingViewService.getTotalViews(listing);
    assertEquals(1L, viewCount);
}

@Test
public void testDuplicatePrevention() {
    // Test duplicate view prevention
    User viewer = createTestUser();
    Listing listing = createTestListing();
    
    // First view
    listingViewService.trackView(listing, viewer, "192.168.1.1", "Mozilla/5.0");
    
    // Second view (should be prevented)
    listingViewService.trackView(listing, viewer, "192.168.1.1", "Mozilla/5.0");
    
    Long viewCount = listingViewService.getTotalViews(listing);
    assertEquals(1L, viewCount); // Should still be 1
}
```

### Integration Tests

```java
@Test
public void testViewTrackingEndpoint() {
    // Test the view tracking endpoint
    Listing listing = createTestListing();
    
    mockMvc.perform(post("/api/listings/" + listing.getId() + "/view")
            .header("Authorization", "Bearer " + generateToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    
    // Verify view was tracked
    Long viewCount = listingViewService.getTotalViews(listing);
    assertEquals(1L, viewCount);
}
```

## Monitoring & Analytics

### Key Metrics
- **Total Views**: Overall engagement with listings
- **Unique Viewers**: Distinct user engagement
- **View Trends**: Daily/weekly view patterns
- **Conversion Rate**: Views to offers ratio

### Alerts
- **Unusual View Patterns**: Detect potential spam
- **High View Volumes**: Monitor for performance issues
- **Analytics Failures**: Monitor for data integrity issues

## Future Enhancements

### 1. Advanced Analytics
- **Geographic Analytics**: View patterns by location
- **Device Analytics**: View patterns by device type
- **Time-based Analytics**: Peak viewing hours

### 2. Machine Learning
- **View Prediction**: Predict listing popularity
- **Spam Detection**: ML-based spam detection
- **Recommendation Engine**: Based on view patterns

### 3. Real-time Features
- **Live View Counts**: Real-time view updates
- **View Notifications**: Notify sellers of new views
- **Live Analytics**: Real-time analytics dashboard

## Troubleshooting

### Common Issues

1. **Views Not Tracking**
   - Check if user is authenticated
   - Verify IP address detection
   - Check database connectivity

2. **Duplicate Views**
   - Verify time-based deduplication logic
   - Check IP address detection accuracy
   - Review session management

3. **Performance Issues**
   - Check database indexes
   - Monitor query performance
   - Review caching strategy

### Debug Mode

Enable debug logging for view tracking:
```yaml
logging:
  level:
    com.acquireindia.service.ListingViewService: DEBUG
    com.acquireindia.repository.ListingViewRepository: DEBUG
```

## Conclusion

The view tracking system provides comprehensive analytics for business listings while maintaining user privacy and preventing spam. The implementation is scalable, secure, and provides valuable insights for sellers to understand listing performance. 