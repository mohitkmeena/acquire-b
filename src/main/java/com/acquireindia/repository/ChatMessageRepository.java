package com.acquireindia.repository;

import com.acquireindia.model.ChatMessage;
import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1)) " +
           "AND m.listing = ?3 ORDER BY m.createdAt ASC")
    List<ChatMessage> findChatMessages(User user1, User user2, Listing listing);
    
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1)) " +
           "AND m.listing = ?3 ORDER BY m.createdAt DESC")
    Page<ChatMessage> findChatMessages(User user1, User user2, Listing listing, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver = ?1 AND m.isRead = false")
    Long countUnreadMessages(User receiver);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.receiver = ?1 AND m.isRead = false")
    List<ChatMessage> findUnreadMessages(User receiver);
    
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.receiver = ?1 " +
           "UNION SELECT DISTINCT m.receiver FROM ChatMessage m WHERE m.sender = ?1")
    List<User> findChatUsers(User user);
}