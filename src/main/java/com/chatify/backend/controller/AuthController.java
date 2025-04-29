package com.chatify.backend.controller;

import com.chatify.backend.dto.AuthRequest;
import com.chatify.backend.dto.ProfileUpdateRequest;
import com.chatify.backend.model.User;
import com.chatify.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserWebSocketController userWebSocketController;

    public AuthController(AuthService authService, UserWebSocketController userWebSocketController) {
        this.authService = authService;
        this.userWebSocketController = userWebSocketController;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest request) {
        User user = authService.register(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody AuthRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Updates the user's profile information (username and language).
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User updated = authService.updateProfile(
            request.getId(), 
            request.getUsername(), 
            request.getLanguage(),
            request.getProfileImageUrl());
        
        // Broadcast the update to all connected clients via WebSocket
        userWebSocketController.broadcastUserUpdate(updated);
        
        return ResponseEntity.ok(updated);
    }
} 