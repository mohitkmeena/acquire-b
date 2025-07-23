package com.acquireindia.repository;

import com.acquireindia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByKycStatus(User.KycStatus kycStatus);
    
    @Query("SELECT u FROM User u WHERE u.kycStatus = ?1")
    Page<User> findByKycStatus(User.KycStatus kycStatus, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.status = ?1")
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %?1% OR u.email LIKE %?1%")
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String keyword, Pageable pageable);
}