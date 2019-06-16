package com.tommy.lobby.controller;

import com.tommy.lobby.models.ChatParticipant;
import com.tommy.lobby.models.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyWSController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/lobby.{username}")
    @SendTo("/openchats/lobby.{username}")
    public void sendPersonalMessage(ChatRoom chatRoom){
        System.out.println("[Message received] : " + chatRoom.toString());

        for (ChatParticipant participant: chatRoom.getParticipants()){
            messagingTemplate.convertAndSend("/openchats/lobby." + participant.getEmail(),chatRoom);
        }
    }
}
