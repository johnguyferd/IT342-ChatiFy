package com.chatify.backend.controller;

import com.chatify.backend.model.User;
import com.chatify.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final Path fileStorageLocation;
    private final UserWebSocketController userWebSocketController;

    public UserController(UserRepository userRepository, UserWebSocketController userWebSocketController) {
        this.userRepository = userRepository;
        this.fileStorageLocation = Paths.get("uploads/avatars").toAbsolutePath().normalize();
        this.userWebSocketController = userWebSocketController;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create directory for uploaded files", ex);
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
    }

    /**
     * Get a single user by their ID (uid).
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setPassword(null);
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Upload a user's avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        
        try {
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Save the file
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user's profile image URL
            return userRepository.findById(userId)
                    .map(user -> {
                        user.setProfileImageUrl("/api/uploads/avatars/" + filename);
                        User updatedUser = userRepository.save(user);
                        
                        // Broadcast the avatar update via WebSocket
                        userWebSocketController.broadcastUserUpdate(updatedUser);
                        
                        Map<String, String> response = new HashMap<>();
                        response.put("profileImageUrl", user.getProfileImageUrl());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file", ex);
        }
    }
    
    /**
     * Delete a user's avatar
     */
    @DeleteMapping("/avatar/{filename:.+}")
    public ResponseEntity<Void> deleteAvatar(@PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file", ex);
        }
    }
} 