package com.chatify.backend.controller;

import com.chatify.backend.model.GroupMember;
import com.chatify.backend.model.ChatGroup;
import com.chatify.backend.repository.GroupMemberRepository;
import com.chatify.backend.repository.ChatGroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/group-members")
public class GroupMemberController {

    private final GroupMemberRepository groupMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatGroupRepository chatGroupRepository;

    public GroupMemberController(GroupMemberRepository groupMemberRepository,
                                 SimpMessagingTemplate messagingTemplate,
                                 ChatGroupRepository chatGroupRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatGroupRepository = chatGroupRepository;
    }

    @GetMapping("/user/{userId}")
    public List<GroupMember> getByUser(@PathVariable String userId) {
        return groupMemberRepository.findByUserId(userId);
    }

    @GetMapping("/group/{groupId}")
    public List<GroupMember> getByGroup(@PathVariable String groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    @PostMapping
    public ResponseEntity<GroupMember> addMember(@RequestBody GroupMember member) {
        GroupMember saved = groupMemberRepository.save(member);
        ChatGroup group = chatGroupRepository.findById(saved.getGroupId()).orElse(null);
        if (group != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "added");
            payload.put("group", group);
            messagingTemplate.convertAndSend("/user/" + saved.getUserId() + "/groups", payload);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeMember(@PathVariable String id) {
        Optional<GroupMember> existingOpt = groupMemberRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GroupMember member = existingOpt.get();
        groupMemberRepository.delete(member);
        ChatGroup group = chatGroupRepository.findById(member.getGroupId()).orElse(null);
        if (group != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "removed");
            payload.put("groupId", member.getGroupId());
            messagingTemplate.convertAndSend("/user/" + member.getUserId() + "/groups", payload);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}/{userId}")
    public ResponseEntity<Void> removeMemberByGroupAndUser(
            @PathVariable String groupId,
            @PathVariable String userId) {
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (members.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GroupMember member = members.get(0);
        groupMemberRepository.delete(member);
        ChatGroup group = chatGroupRepository.findById(groupId).orElse(null);
        if (group != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "removed");
            payload.put("groupId", groupId);
            messagingTemplate.convertAndSend("/user/" + userId + "/groups", payload);
        }
        return ResponseEntity.noContent().build();
    }

    // Update member role by groupId and userId
    @PatchMapping("/{groupId}/{userId}")
    public ResponseEntity<GroupMember> updateMemberRole(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestBody Map<String, String> updates) {
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (members.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GroupMember member = members.get(0);
        String role = updates.get("role");
        if (role != null && !role.trim().isEmpty()) {
            member.setRole(role.trim());
            GroupMember updated = groupMemberRepository.save(member);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.badRequest().build();
    }
}