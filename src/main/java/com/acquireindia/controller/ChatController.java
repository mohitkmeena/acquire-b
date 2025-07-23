package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.ChatMessage;
import com.acquireindia.model.Listing;
import com.acquireindia.model.User;
import com.acquireindia.service.ChatService;
import com.acquireindia.service.ListingService;
import com.acquireindia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ListingService listingService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/chat.sendMessage/{listingId}")
    public void sendMessage(@DestinationVariable Long listingId,
                           @Payload Map<String, Object> messagePayload,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            String senderEmail = headerAccessor.getUser().getName();
            User sender = userService.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
            
            Long receiverId = Long.valueOf(messagePayload.get("receiverId").toString());
            User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
            
            Listing listing = listingService.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
            
            String content = messagePayload.get("content").toString();
            
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setSender(sender);
            chatMessage.setReceiver(receiver);
            chatMessage.setListing(listing);
            
            if (messagePayload.containsKey("type")) {
                chatMessage.setType(ChatMessage.MessageType.valueOf(messagePayload.get("type").toString()));
            }
            
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);
            
            // Send message to receiver
            messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                savedMessage
            );
            
            // Send confirmation to sender
            messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/messages",
                savedMessage
            );
            
        } catch (Exception e) {
            // Handle error
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/errors",
                "Failed to send message: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/messages/{listingId}/{userId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatMessages(@PathVariable Long listingId,
                                                                          @PathVariable Long userId,
                                                                          Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            User otherUser = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));
            
            Listing listing = listingService.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
            
            List<ChatMessage> messages = chatService.getChatMessages(currentUser, otherUser, listing);
            
            // Mark messages as read
            chatService.markMessagesAsRead(otherUser, currentUser, listing);
            
            return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve messages: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<User>>> getChatUsers(Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<User> chatUsers = chatService.getChatUsers(currentUser);
            return ResponseEntity.ok(ApiResponse.success("Chat users retrieved successfully", chatUsers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve chat users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(Authentication authentication) {
        try {
            User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long unreadCount = chatService.getUnreadMessageCount(currentUser);
            return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve unread count: " + e.getMessage()));
        }
    }
}