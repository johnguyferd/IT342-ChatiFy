package com.chatify.backend.controller;

import com.chatify.backend.model.User;
import com.chatify.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final UserRepository userRepository;
    private final UserWebSocketController userWebSocketController;

    public StatusController(UserRepository userRepository, UserWebSocketController userWebSocketController) {
        this.userRepository = userRepository;
        this.userWebSocketController = userWebSocketController;
    }

    @GetMapping
    public Map<String, String> getStatus() {
        // Map each user ID to its current status
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getStatus));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(statusUpdate.get("status"));
                    User updated = userRepository.save(user);
                    
                    // Broadcast the status update to all connected clients
                    userWebSocketController.broadcastUserUpdate(updated);
                    
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 