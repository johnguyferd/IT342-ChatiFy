package com.chatify.backend.service;

import com.chatify.backend.model.User;
import com.chatify.backend.repository.UserRepository;
import com.chatify.backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProfileImageUrl("none");
        user.setAccountType("free");
        user.setLanguage(null);
        user.setTranslator("google");
        User saved = userRepository.save(user);
        return sanitizeUser(saved);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        User sanitized = sanitizeUser(user);
        
        // Generate JWT token and add it to the user object
        String token = tokenProvider.generateToken(user.getId());
        sanitized.setToken(token);
        
        return sanitized;
    }

    /**
     * Updates a user's profile (username and language).
     */
    public User updateProfile(String id, String username, String language) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(username);
        user.setLanguage(language);
        User updated = userRepository.save(user);
        return sanitizeUser(updated);
    }

    public User updateProfile(String id, String username, String language, String profileImageUrl) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(username);
        user.setLanguage(language);
        if (profileImageUrl != null) {
            user.setProfileImageUrl(profileImageUrl);
        }
        User updated = userRepository.save(user);
        return sanitizeUser(updated);
    }

    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }
} 