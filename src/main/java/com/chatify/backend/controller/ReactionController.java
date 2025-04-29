package com.chatify.backend.controller;

import com.chatify.backend.model.Reaction;
import com.chatify.backend.model.Message;
import com.chatify.backend.repository.ReactionRepository;
import com.chatify.backend.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    private final ReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ReactionController(
            ReactionRepository reactionRepository,
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.reactionRepository = reactionRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Get all reactions for a given chatId and messageId
     */
    @GetMapping("/{chatId}/{messageId}")
    public List<Reaction> getReactions(
            @PathVariable String chatId,
            @PathVariable String messageId) {
        return reactionRepository.findByChatIdAndMessageId(chatId, messageId);
    }

    /**
     * Add a new reaction
     */
    @PostMapping
    public ResponseEntity<Reaction> addReaction(@RequestBody Reaction reaction) {
        // First, save the reaction
        Reaction saved = reactionRepository.save(reaction);
        
        // Then retrieve the message to update its reactions list
        Optional<Message> messageOpt = messageRepository.findById(reaction.getMessageId());
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            
            // Update the message with the new reaction
            if (message.getReactions() == null) {
                message.setReactions(new HashMap<>());
            }
            message.getReactions().put(saved.getId(), saved);
            
            // Save the updated message
            Message updatedMessage = messageRepository.save(message);
            
            // Broadcast the updated message via WebSocket
            messagingTemplate.convertAndSend("/topic/messages/" + message.getChatId(), updatedMessage);
        }
        
        return ResponseEntity.ok(saved);
    }

    /**
     * Remove a reaction by id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeReaction(@PathVariable String id) {
        // First, get the reaction to find its message
        Optional<Reaction> reactionOpt = reactionRepository.findById(id);
        
        if (reactionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Reaction reaction = reactionOpt.get();
        String messageId = reaction.getMessageId();
        String chatId = reaction.getChatId();
        
        // Delete the reaction
        reactionRepository.deleteById(id);
        
        // Update the message
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            
            // Remove the reaction from the message
            if (message.getReactions() != null && message.getReactions().containsKey(id)) {
                message.getReactions().remove(id);
            }
            
            // Save the updated message
            Message updatedMessage = messageRepository.save(message);
            
            // Broadcast the updated message via WebSocket
            messagingTemplate.convertAndSend("/topic/messages/" + chatId, updatedMessage);
        }
        
        return ResponseEntity.noContent().build();
    }
} 