package com.acquireindia.service;

import com.acquireindia.model.ChatMessage;
import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import com.acquireindia.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }
    
    public List<ChatMessage> getChatMessages(User user1, User user2, Listing listing) {
        return chatMessageRepository.findChatMessages(user1, user2, listing);
    }
    
    public Page<ChatMessage> getChatMessages(User user1, User user2, Listing listing, Pageable pageable) {
        return chatMessageRepository.findChatMessages(user1, user2, listing, pageable);
    }
    
    public Long getUnreadMessageCount(User receiver) {
        return chatMessageRepository.countUnreadMessages(receiver);
    }
    
    public List<ChatMessage> getUnreadMessages(User receiver) {
        return chatMessageRepository.findUnreadMessages(receiver);
    }
    
    public List<User> getChatUsers(User user) {
        return chatMessageRepository.findChatUsers(user);
    }
    
    public void markMessageAsRead(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setRead(true);
            chatMessageRepository.save(message);
        }
    }
    
    public void markMessagesAsRead(User sender, User receiver, Listing listing) {
        List<ChatMessage> messages = chatMessageRepository.findChatMessages(sender, receiver, listing);
        messages.forEach(message -> {
            if (!message.isRead() && message.getReceiver().equals(receiver)) {
                message.setRead(true);
            }
        });
        chatMessageRepository.saveAll(messages);
    }
}