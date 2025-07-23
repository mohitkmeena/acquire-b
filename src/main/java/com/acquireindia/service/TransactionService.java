package com.acquireindia.service;

import com.acquireindia.model.Transaction;
import com.acquireindia.model.User;
import com.acquireindia.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public Optional<Transaction> findByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }
    
    public Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId) {
        return transactionRepository.findByRazorpayOrderId(razorpayOrderId);
    }
    
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> findByBuyer(User buyer) {
        return transactionRepository.findByBuyer(buyer);
    }
    
    public List<Transaction> findBySeller(User seller) {
        return transactionRepository.findBySeller(seller);
    }
    
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
    
    public Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable);
    }
    
    public Page<Transaction> findAllOrderByCreatedAtDesc(Pageable pageable) {
        return transactionRepository.findAllOrderByCreatedAtDesc(pageable);
    }
    
    public Transaction updateTransactionStatus(Long transactionId, Transaction.TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }
}