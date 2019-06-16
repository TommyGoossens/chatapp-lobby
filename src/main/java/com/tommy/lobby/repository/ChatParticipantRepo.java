package com.tommy.lobby.repository;

import com.tommy.lobby.models.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatParticipantRepo extends JpaRepository<ChatParticipant,Long> {
    Optional<ChatParticipant> findChatParticipantByEmail(String email);
}
