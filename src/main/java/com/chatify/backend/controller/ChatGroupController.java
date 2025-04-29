package com.chatify.backend.controller;

import com.chatify.backend.model.ChatGroup;
import com.chatify.backend.repository.ChatGroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class ChatGroupController {

    private final ChatGroupRepository chatGroupRepository;

    public ChatGroupController(ChatGroupRepository chatGroupRepository) {
        this.chatGroupRepository = chatGroupRepository;
    }

    @GetMapping
    public List<ChatGroup> getAllGroups() {
        return chatGroupRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatGroup> getGroupById(@PathVariable String id) {
        return chatGroupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChatGroup> createGroup(@RequestBody ChatGroup group) {
        ChatGroup saved = chatGroupRepository.save(group);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChatGroup> updateGroupName(
            @PathVariable String id,
            @RequestBody Map<String, String> updates) {
        return chatGroupRepository.findById(id)
                .map(group -> {
                    String name = updates.get("name");
                    if (name != null && !name.trim().isEmpty()) {
                        group.setName(name.trim());
                    }
                    ChatGroup updated = chatGroupRepository.save(group);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 