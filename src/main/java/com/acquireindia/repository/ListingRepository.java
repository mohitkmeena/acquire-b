package com.acquireindia.repository;

import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findBySeller(User seller);
    
    List<Listing> findByStatus(Listing.ListingStatus status);
    
    Page<Listing> findByStatus(Listing.ListingStatus status, Pageable pageable);
    
    List<Listing> findByCategory(String category);
    
    Page<Listing> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' OR l.status = 'NEGOTIABLE'")
    Page<Listing> findActiveListings(Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE " +
           "(l.status = 'ACTIVE' OR l.status = 'NEGOTIABLE') AND " +
           "(:category IS NULL OR l.category = :category) AND " +
           "(:minPrice IS NULL OR l.askingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.askingPrice <= :maxPrice) AND " +
           "(:location IS NULL OR l.location LIKE %:location%) AND " +
           "(:keyword IS NULL OR l.title LIKE %:keyword% OR l.description LIKE %:keyword%)")
    Page<Listing> findListingsWithFilters(
        @Param("category") String category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("location") String location,
        @Param("keyword") String keyword,
        Pageable pageable);
    
    @Query("SELECT DISTINCT l.category FROM Listing l WHERE l.status = 'ACTIVE' OR l.status = 'NEGOTIABLE'")
    List<String> findDistinctCategories();
    
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.seller = ?1")
    Long countBySeller(User seller);
}