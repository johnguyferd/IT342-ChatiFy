package com.chatify.backend.repository;

import com.chatify.backend.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, String> {
    List<Reaction> findByChatId(String chatId);
    List<Reaction> findByMessageId(String messageId);
    List<Reaction> findByChatIdAndMessageId(String chatId, String messageId);
} 