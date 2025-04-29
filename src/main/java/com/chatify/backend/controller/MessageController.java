package com.chatify.backend.controller;

import com.chatify.backend.model.Message;
import com.chatify.backend.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final Path fileStorageLocation;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public MessageController(MessageRepository messageRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.messageRepository = messageRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.fileStorageLocation = Paths.get("uploads/images").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create directory for uploaded files", ex);
        }
    }

    /**
     * Fetch all messages for a given chat (direct or group) by chatId.
     */
    @GetMapping("/{chatId}")
    public List<Message> getMessagesByChatId(@PathVariable String chatId) {
        return messageRepository.findByChatId(chatId);
    }

    /**
     * Fetch a specific message by chatId and messageId.
     */
    @GetMapping("/{chatId}/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable String chatId, @PathVariable String messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        return message.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Post a new message to the chat.
     */
    @PostMapping
    public ResponseEntity<Message> postMessage(@RequestBody Message message) {
        message.setTimestamp(LocalDateTime.now());
        Message saved = messageRepository.save(message);
        
        // Send to chat topic for real-time chat updates
        simpMessagingTemplate.convertAndSend("/topic/messages/" + saved.getChatId(), saved);
        
        // Also send to user-specific topics for sidebar updates
        if (saved.getChatId().contains("_")) {
            // For direct messages (format: "user1_user2")
            String[] userIds = saved.getChatId().split("_");
            for (String userId : userIds) {
                if (!userId.equals(saved.getSenderId())) {
                    // Send to the recipient's user-specific topic
                    simpMessagingTemplate.convertAndSend("/user/" + userId + "/messages", saved);
                }
            }
        } else {
            // For group messages, we would ideally get all members and send to each
            // This would require injecting a group member service
            // Implementation depends on how group membership is managed
        }
        
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing message with translations
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(@PathVariable String messageId, @RequestBody Message message) {
        if (!messageId.equals(message.getMessageId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        Optional<Message> existingMessage = messageRepository.findById(messageId);
        if (existingMessage.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Preserve creation timestamp
        message.setTimestamp(existingMessage.get().getTimestamp());
        
        Message updated = messageRepository.save(message);
        simpMessagingTemplate.convertAndSend("/topic/messages/" + updated.getChatId(), updated);
        return ResponseEntity.ok(updated);
    }

    /**
     * Upload an image message
     */
    @PostMapping("/image")
    public ResponseEntity<Message> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chatId") String chatId,
            @RequestParam("senderId") String senderId,
            @RequestParam(value = "replyToMessageId", required = false) String replyToMessageId,
            @RequestParam(value = "replyToSenderId", required = false) String replyToSenderId,
            @RequestParam(value = "replyToMessage", required = false) String replyToMessage,
            @RequestParam(value = "replyToType", required = false) String replyToType) {
        
        try {
            // Generate file name
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Copy file to storage location
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Create message object
            Message message = new Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setChatId(chatId);
            message.setSenderId(senderId);
            message.setType("image");
            message.setImageUrl("/api/uploads/images/" + filename);
            message.setTimestamp(LocalDateTime.now());
            
            // Handle reply data if present
            if (replyToMessageId != null && replyToSenderId != null) {
                // You would need to implement a proper nested object for replyTo in your Message entity
                // This is a simplified approach
                message.setReplyToMessageId(replyToMessageId);
                message.setReplyToSenderId(replyToSenderId);
                message.setReplyToMessage(replyToMessage);
                message.setReplyToType(replyToType);
            }
            
            Message saved = messageRepository.save(message);
            
            // Send to chat topic for real-time updates
            simpMessagingTemplate.convertAndSend("/topic/messages/" + chatId, saved);
            
            // Also send to user-specific topics for sidebar updates
            if (chatId.contains("_")) {
                // For direct messages (format: "user1_user2")
                String[] userIds = chatId.split("_");
                for (String userId : userIds) {
                    if (!userId.equals(senderId)) {
                        // Send to the recipient's user-specific topic
                        simpMessagingTemplate.convertAndSend("/user/" + userId + "/messages", saved);
                    }
                }
            } else {
                // For group messages, we would ideally get all members and send to each
                // This would require injecting a group member service
            }
            
            return ResponseEntity.ok(saved);
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file", ex);
        }
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable String chatId, @RequestBody Map<String, String> payload) {
        String receiverId = payload.get("receiverId");
        List<Message> messages = messageRepository.findByChatId(chatId);
        
        for (Message message : messages) {
            if (!message.getSenderId().equals(receiverId) && !message.isRead()) {
                message.setIsReadMessage(true);
                Message updated = messageRepository.save(message);
                simpMessagingTemplate.convertAndSend("/topic/messages/" + chatId, updated);
            }
        }
        
        return ResponseEntity.ok().build();
    }

    /**
     * Regenerate translation for a message
     */
    @PutMapping("/{messageId}/regenerate")
    public ResponseEntity<Message> regenerateTranslation(@PathVariable String messageId, @RequestBody Map<String, String> payload) {
        String variation = payload.get("variation");
        
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Message message = messageOptional.get();
        // Get the content from the specified variation
        String newTranslation = null;
        if ("messageVar1".equals(variation) && message.getMessageVar1() != null) {
            newTranslation = message.getMessageVar1();
        } else if ("messageVar2".equals(variation) && message.getMessageVar2() != null) {
            newTranslation = message.getMessageVar2();
        } else if ("messageVar3".equals(variation) && message.getMessageVar3() != null) {
            newTranslation = message.getMessageVar3();
        }
        
        if (newTranslation != null) {
            message.setMessage(newTranslation);
            Message updated = messageRepository.save(message);
            simpMessagingTemplate.convertAndSend("/topic/messages/" + updated.getChatId(), updated);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
} 