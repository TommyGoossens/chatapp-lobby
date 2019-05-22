package com.tommy.lobby.controller;

import com.tommy.lobby.models.ChatParticipant;
import com.tommy.lobby.models.ChatRoom;
import com.tommy.lobby.repository.ChatParticipantRepo;
import com.tommy.lobby.repository.ChatRoomRepo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/lobby")
@Api(value = "/profile", produces = "application/json")
public class LobbyRestController {

    private final ChatParticipantRepo participantRepo;
    private final ChatRoomRepo chatRoomRepo;

    @Autowired
    public LobbyRestController(ChatParticipantRepo participantRepo, ChatRoomRepo chatRoomRepo) {
        this.participantRepo = participantRepo;
        this.chatRoomRepo = chatRoomRepo;
    }


    @GetMapping("/chats")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllActiveChats() {
        ChatParticipant participant = participantRepo.findChatParticipantByUsername(getUsername()).orElseThrow(RuntimeException::new);

        List<ChatRoom> rooms = new ArrayList<>();

        for(Long id : participant.getChatRooms()){
            rooms.add(chatRoomRepo.getOne(id));
        }

        return ResponseEntity.status(200).body(rooms);
    }

    public String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }
}
