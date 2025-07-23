package com.acquireindia.repository;

import com.acquireindia.model.Transaction;
import com.acquireindia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.offer.buyer = ?1")
    List<Transaction> findByBuyer(User buyer);
    
    @Query("SELECT t FROM Transaction t WHERE t.offer.listing.seller = ?1")
    List<Transaction> findBySeller(User seller);
    
    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    Page<Transaction> findAllOrderByCreatedAtDesc(Pageable pageable);
}