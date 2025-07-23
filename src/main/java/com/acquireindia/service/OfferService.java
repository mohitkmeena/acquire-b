package com.acquireindia.service;

import com.acquireindia.model.Listing;
import com.acquireindia.model.Offer;
import com.acquireindia.model.User;
import com.acquireindia.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferService {
    
    @Autowired
    private OfferRepository offerRepository;
    
    public Offer createOffer(Offer offer) {
        return offerRepository.save(offer);
    }
    
    public Optional<Offer> findById(Long id) {
        return offerRepository.findById(id);
    }
    
    public List<Offer> findByBuyer(User buyer) {
        return offerRepository.findByBuyer(buyer);
    }
    
    public List<Offer> findByListing(Listing listing) {
        return offerRepository.findByListing(listing);
    }
    
    public List<Offer> findByListingSeller(User seller) {
        return offerRepository.findByListingSeller(seller);
    }
    
    public Page<Offer> findByBuyer(User buyer, Pageable pageable) {
        return offerRepository.findByBuyer(buyer, pageable);
    }
    
    public Page<Offer> findByListingSeller(User seller, Pageable pageable) {
        return offerRepository.findByListingSeller(seller, pageable);
    }
    
    public Offer updateOffer(Offer offer) {
        return offerRepository.save(offer);
    }
    
    public Offer updateOfferStatus(Long offerId, Offer.OfferStatus status) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setStatus(status);
        return offerRepository.save(offer);
    }
    
    public Optional<Offer> findByBuyerAndListing(User buyer, Listing listing) {
        return offerRepository.findByBuyerAndListing(buyer, listing);
    }
    
    public List<Offer> findBySellerAndStatus(User seller, Offer.OfferStatus status) {
        return offerRepository.findBySellerAndStatus(seller, status);
    }
    
    public Long countByBuyer(User buyer) {
        return offerRepository.countByBuyer(buyer);
    }
    
    public Long countBySeller(User seller) {
        return offerRepository.countBySeller(seller);
    }
    
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }
}