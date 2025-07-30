package com.acquireindia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_views")
public class ListingView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    private User viewer;
    
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    
    @Column(nullable = false)
    private LocalDateTime viewedAt;
    
    // For anonymous views
    private String anonymousId;
    
    public ListingView() {}
    
    public ListingView(Listing listing, User viewer, String ipAddress, String userAgent) {
        this.listing = listing;
        this.viewer = viewer;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.viewedAt = LocalDateTime.now();
    }
    
    public ListingView(Listing listing, String ipAddress, String userAgent, String sessionId) {
        this.listing = listing;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.viewedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }
    
    public User getViewer() { return viewer; }
    public void setViewer(User viewer) { this.viewer = viewer; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
    
    public String getAnonymousId() { return anonymousId; }
    public void setAnonymousId(String anonymousId) { this.anonymousId = anonymousId; }
} 