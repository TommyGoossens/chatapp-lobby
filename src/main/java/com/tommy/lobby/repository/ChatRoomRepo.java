package com.tommy.lobby.repository;

import com.tommy.lobby.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepo extends JpaRepository<ChatRoom,Long> {
    Optional<ChatRoom> findAllById(Long id);
}
