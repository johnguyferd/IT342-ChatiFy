package com.chatify.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// In TestController.java
@RestController
@RequestMapping("/api/test") // Change the base path
public class TestController {

    @GetMapping("/ping") // This will now be at /api/test/ping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Test controller ping successful!");
    }
}