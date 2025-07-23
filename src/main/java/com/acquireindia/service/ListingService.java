package com.acquireindia.service;

import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import com.acquireindia.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {
    
    @Autowired
    private ListingRepository listingRepository;
    
    public Listing createListing(Listing listing) {
        return listingRepository.save(listing);
    }
    
    public Optional<Listing> findById(Long id) {
        return listingRepository.findById(id);
    }
    
    public Page<Listing> findActiveListings(Pageable pageable) {
        return listingRepository.findActiveListings(pageable);
    }
    
    public Page<Listing> findListingsWithFilters(String category, BigDecimal minPrice, 
                                                  BigDecimal maxPrice, String location, 
                                                  String keyword, Pageable pageable) {
        return listingRepository.findListingsWithFilters(category, minPrice, maxPrice, location, keyword, pageable);
    }
    
    public List<Listing> findBySeller(User seller) {
        return listingRepository.findBySeller(seller);
    }
    
    public Listing updateListing(Listing listing) {
        return listingRepository.save(listing);
    }
    
    public void deleteListing(Long id) {
        listingRepository.deleteById(id);
    }
    
    public List<String> getDistinctCategories() {
        return listingRepository.findDistinctCategories();
    }
    
    public List<Listing> findByCategory(String category) {
        return listingRepository.findByCategory(category);
    }
    
    public List<Listing> findByStatus(Listing.ListingStatus status) {
        return listingRepository.findByStatus(status);
    }
    
    public Long countBySeller(User seller) {
        return listingRepository.countBySeller(seller);
    }
    
    public Listing updateListingStatus(Long listingId, Listing.ListingStatus status) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        listing.setStatus(status);
        return listingRepository.save(listing);
    }
}