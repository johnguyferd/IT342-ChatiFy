package com.chatify.backend.controller;

import com.chatify.backend.model.Message;
import java.util.List;
import com.chatify.backend.repository.MessageRepository;
import com.chatify.backend.repository.GroupMemberRepository;
import com.chatify.backend.model.GroupMember;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   MessageRepository messageRepository,
                                   GroupMemberRepository groupMemberRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload Message message) {
        // Persist the message to the database
        Message saved = messageRepository.save(message);
        
        // Broadcast the saved message to all subscribers of the chat topic
        messagingTemplate.convertAndSend(
                "/topic/messages/" + saved.getChatId(),
                saved
        );
        
        // Also send to the user-specific topic for sidebar updates
        // For direct messages, format is "user1_user2"
        if (saved.getChatId().contains("_")) {
            String[] userIds = saved.getChatId().split("_");
            for (String userId : userIds) {
                if (!userId.equals(saved.getSenderId())) {
                    // Send to the recipient's user-specific topic for sidebar updates
                    messagingTemplate.convertAndSend("/user/" + userId + "/messages", saved);
                }
            }
        } else {
            List<GroupMember> members = groupMemberRepository.findByGroupId(saved.getChatId());
            for (GroupMember member : members) {
                String userId = member.getUserId();
                if (!userId.equals(saved.getSenderId())) {
                    messagingTemplate.convertAndSend("/user/" + userId + "/messages", saved);
                }
            }
        }
    }
} 