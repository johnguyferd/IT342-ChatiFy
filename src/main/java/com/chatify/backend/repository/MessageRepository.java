package com.chatify.backend.repository;

import com.chatify.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChatId(String chatId);
} 