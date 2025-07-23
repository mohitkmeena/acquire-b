package com.acquireindia.repository;

import com.acquireindia.model.Listing;
import com.acquireindia.model.SavedListing;
import com.acquireindia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, Long> {
    List<SavedListing> findByUser(User user);
    
    Optional<SavedListing> findByUserAndListing(User user, Listing listing);
    
    boolean existsByUserAndListing(User user, Listing listing);
    
    void deleteByUserAndListing(User user, Listing listing);
    
    Long countByUser(User user);
}