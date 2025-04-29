package com.chatify.backend.repository;

import com.chatify.backend.model.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, String> {
} 