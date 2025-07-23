package com.acquireindia.service;

import com.acquireindia.model.Listing;
import com.acquireindia.model.SavedListing;
import com.acquireindia.model.User;
import com.acquireindia.repository.SavedListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SavedListingService {
    
    @Autowired
    private SavedListingRepository savedListingRepository;
    
    public SavedListing saveListing(User user, Listing listing, String notes) {
        if (savedListingRepository.existsByUserAndListing(user, listing)) {
            throw new RuntimeException("Listing already saved");
        }
        
        SavedListing savedListing = new SavedListing(user, listing, notes);
        return savedListingRepository.save(savedListing);
    }
    
    public List<SavedListing> findByUser(User user) {
        return savedListingRepository.findByUser(user);
    }
    
    public Optional<SavedListing> findByUserAndListing(User user, Listing listing) {
        return savedListingRepository.findByUserAndListing(user, listing);
    }
    
    @Transactional
    public void removeSavedListing(User user, Listing listing) {
        savedListingRepository.deleteByUserAndListing(user, listing);
    }
    
    public boolean isListingSaved(User user, Listing listing) {
        return savedListingRepository.existsByUserAndListing(user, listing);
    }
    
    public Long countByUser(User user) {
        return savedListingRepository.countByUser(user);
    }
}