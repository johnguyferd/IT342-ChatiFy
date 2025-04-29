package com.chatify.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/uploads")
public class FileController {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/images").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                HttpHeaders headers = new HttpHeaders();
                // Set content type based on file extension
                String contentType = determineContentType(filename);
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
                
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<Resource> serveAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/avatars").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                HttpHeaders headers = new HttpHeaders();
                // Set content type based on file extension
                String contentType = determineContentType(filename);
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    private String determineContentType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseFilename.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
} 