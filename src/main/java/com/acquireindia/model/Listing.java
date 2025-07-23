package com.acquireindia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "listings")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String title;
    
    @Lob
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal revenue;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal askingPrice;
    
    @NotBlank
    private String category;
    
    private String website;
    private String demoVideoUrl;
    
    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;
    
    private String location;
    private Integer employeeCount;
    private String businessModel;
    
    @Lob
    private String financialDetails;
    
    // Additional fields to match frontend requirements
    private BigDecimal monthlyProfit;
    private Integer yearEstablished;
    private String reasonForSelling;
    private String assetsIncluded;
    private String growthOpportunities;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Offer> offers;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // Constructors
    public Listing() {}

    public Listing(String title, String description, BigDecimal revenue, BigDecimal askingPrice, 
                   String category, User seller) {
        this.title = title;
        this.description = description;
        this.revenue = revenue;
        this.askingPrice = askingPrice;
        this.category = category;
        this.seller = seller;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    
    public BigDecimal getAskingPrice() { return askingPrice; }
    public void setAskingPrice(BigDecimal askingPrice) { this.askingPrice = askingPrice; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getDemoVideoUrl() { return demoVideoUrl; }
    public void setDemoVideoUrl(String demoVideoUrl) { this.demoVideoUrl = demoVideoUrl; }
    
    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }
    
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
    
    public String getBusinessModel() { return businessModel; }
    public void setBusinessModel(String businessModel) { this.businessModel = businessModel; }
    
    public String getFinancialDetails() { return financialDetails; }
    public void setFinancialDetails(String financialDetails) { this.financialDetails = financialDetails; }
    
    public BigDecimal getMonthlyProfit() { return monthlyProfit; }
    public void setMonthlyProfit(BigDecimal monthlyProfit) { this.monthlyProfit = monthlyProfit; }
    
    public Integer getYearEstablished() { return yearEstablished; }
    public void setYearEstablished(Integer yearEstablished) { this.yearEstablished = yearEstablished; }
    
    public String getReasonForSelling() { return reasonForSelling; }
    public void setReasonForSelling(String reasonForSelling) { this.reasonForSelling = reasonForSelling; }
    
    public String getAssetsIncluded() { return assetsIncluded; }
    public void setAssetsIncluded(String assetsIncluded) { this.assetsIncluded = assetsIncluded; }
    
    public String getGrowthOpportunities() { return growthOpportunities; }
    public void setGrowthOpportunities(String growthOpportunities) { this.growthOpportunities = growthOpportunities; }
    
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    
    public List<Offer> getOffers() { return offers; }
    public void setOffers(List<Offer> offers) { this.offers = offers; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Enums
    public enum ListingStatus {
        ACTIVE, NEGOTIABLE, SOLD, INACTIVE
    }

    public enum BusinessType {
        SAAS, ECOMMERCE, MOBILE_APP, MARKETPLACE, BLOG, SERVICE_BUSINESS, MANUFACTURING, OTHER
    }
}