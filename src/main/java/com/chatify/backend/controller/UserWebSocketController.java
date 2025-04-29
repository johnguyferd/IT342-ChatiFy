package com.chatify.backend.controller;

import com.chatify.backend.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public UserWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast user profile updates to all clients
     */
    public void broadcastUserUpdate(User user) {
        // Create a map with user data instead of trying to create a new User object
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("language", user.getLanguage());
        userData.put("status", user.getStatus());
        
        // Broadcast to a topic that clients can subscribe to
        messagingTemplate.convertAndSend("/topic/users/" + user.getId(), userData);
        // Also broadcast to a general topic for all user updates
        messagingTemplate.convertAndSend("/topic/users", userData);
    }
} 