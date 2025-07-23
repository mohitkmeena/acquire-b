package com.acquireindia.repository;

import com.acquireindia.model.Listing;
import com.acquireindia.model.Offer;
import com.acquireindia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByBuyer(User buyer);
    
    List<Offer> findByListing(Listing listing);
    
    List<Offer> findByListingSeller(User seller);
    
    Page<Offer> findByBuyer(User buyer, Pageable pageable);
    
    Page<Offer> findByListingSeller(User seller, Pageable pageable);
    
    List<Offer> findByStatus(Offer.OfferStatus status);
    
    Optional<Offer> findByBuyerAndListing(User buyer, Listing listing);
    
    @Query("SELECT o FROM Offer o WHERE o.listing.seller = ?1 AND o.status = ?2")
    List<Offer> findBySellerAndStatus(User seller, Offer.OfferStatus status);
    
    @Query("SELECT COUNT(o) FROM Offer o WHERE o.buyer = ?1")
    Long countByBuyer(User buyer);
    
    @Query("SELECT COUNT(o) FROM Offer o WHERE o.listing.seller = ?1")
    Long countBySeller(User seller);
}