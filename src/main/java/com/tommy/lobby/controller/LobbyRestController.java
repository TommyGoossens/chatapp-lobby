package com.tommy.lobby.controller;

import com.tommy.lobby.models.*;
import com.tommy.lobby.repository.ChatParticipantRepo;
import com.tommy.lobby.repository.ChatRoomRepo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.EntityNotFoundException;
import java.util.*;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value = "/lobby", produces = "application/json", tags = "Lobby")
public class LobbyRestController {

    private final ChatParticipantRepo participantRepo;
    private final ChatRoomRepo chatRoomRepo;
    private final LobbyWSController websocketController;

    @Autowired
    public LobbyRestController(ChatParticipantRepo participantRepo, ChatRoomRepo chatRoomRepo, LobbyWSController websocketController) {
        this.participantRepo = participantRepo;
        this.chatRoomRepo = chatRoomRepo;
        this.websocketController = websocketController;
    }


    /**
     * Retrieves all chatsessions the user has
     * @return list of chatrooms
     */
    @ApiOperation(value = "Retrieve all open sessions")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Successfully retrieved all chat sessions", response = ChatRoom.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[User does not exist]")
    })
    @GetMapping("/chats")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getAllActiveChats() {
        ChatParticipant participant = participantRepo.findChatParticipantByEmail(getUsername()).orElseThrow(RuntimeException::new);

        List<ChatRoom> rooms = new ArrayList<>();

        for (ChatRoom room : participant.getChatRooms()) {
            rooms.add(chatRoomRepo.getOne(room.getId()));
        }
        return ResponseEntity.status(200).body(rooms);
    }

    /**
     * Creates a mew chatroom
     * @param chatroomDTO object containing the new chat information: participants, group, groupname
     * @return Chatroom object
     */
    @ApiOperation(value = "Create a new chatroom")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "[Chatroom successfully created]", response = ChatRoom.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[User does not exist]")

    })
    @PostMapping("/newchatroom")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createNewChatroom(@RequestBody NewChatroomDTO chatroomDTO) {
        ChatRoom newRoom = new ChatRoom();
        Set<ChatParticipant> participants = new HashSet<>();
        Set<String> admins = new HashSet<>();
        UUID sessionId = UUID.randomUUID();

        for (String participant : chatroomDTO.getParticipants()) {
            ChatParticipant foundParticipant = participantRepo.findChatParticipantByEmail(participant).orElseThrow(RuntimeException::new);
            participants.add(foundParticipant);
            if (isPersonCreator(participant)) admins.add(foundParticipant.getEmail());
        }

        newRoom.setChatroomIdentifier(sessionId.toString());
        newRoom.setParticipants(participants);
        newRoom.setAdmins(admins);
        newRoom.setGroupChat(chatroomDTO.isGroupChat());
        newRoom.setGroupDescription(chatroomDTO.getGroupDescription());
        newRoom = chatRoomRepo.save(newRoom);

        addRoomsToParticipants(newRoom, participants);

        return ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    /**
     * Updates the chatroom information
     * @param updatedRoom object containing the identifier and new information
     * @return the updated room
     */
    @ApiOperation(value = "Update a chatroom")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[Chatroom updated]", response = ChatRoom.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[Object not found]")
    })
    @PutMapping("/updateroom")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateChatroom(@RequestBody ChatRoom updatedRoom) {
        ChatRoom currentRoom = chatRoomRepo.getChatRoomByChatroomIdentifier(updatedRoom.getChatroomIdentifier()).orElseThrow(OBJECT_NOT_EXIST::new);
        currentRoom.updateDetails(updatedRoom);
        chatRoomRepo.save(currentRoom);
        websocketController.sendPersonalMessage(currentRoom);
        return ResponseEntity.status(HttpStatus.OK).body(currentRoom);
    }

    /**
     * Adds a participant to the group
     * @param email from the participant that will be added
     * @return
     */
    @PutMapping("/add/{sessionIdentifier}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getProfile(@PathVariable("sessionIdentifier") String roomIdentifier, @RequestBody String email) {
        ChatRoom room = chatRoomRepo.getChatRoomByChatroomIdentifier(roomIdentifier).orElseThrow(EntityNotFoundException::new);
        if(!room.getAdmins().contains(getUsername())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("[You are not an admin]");
        ChatParticipant participant = participantRepo.findChatParticipantByEmail(email).orElseThrow(RuntimeException::new);
        room.getParticipants().add(participant);

        chatRoomRepo.save(room);
        return ResponseEntity.status(HttpStatus.OK).body(room);
    }

    /**
     * Leave request, removes a user from the chat session
     * @param roomIdentifier UUID from the chat session
     * @return the retrieved room
     */
    @ApiOperation(value = "Leave an existing chat session")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "[User has left the room]", response = ChatRoom.class),
            @ApiResponse(code = 403, message = "[Unauthorized]"),
            @ApiResponse(code = 404, message = "[Object not found]")
    })
    @PutMapping("/leave/{sessionIdentifier}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> leaveRoom(@PathVariable("sessionIdentifier") String roomIdentifier) {
        ChatRoom currentRoom = chatRoomRepo.getChatRoomByChatroomIdentifier(roomIdentifier).orElseThrow(OBJECT_NOT_EXIST::new);
        ChatParticipant participant = participantRepo.findChatParticipantByEmail(getUsername()).orElseThrow(RuntimeException::new);

        currentRoom.leaveRoom(getUsername());
        participant.leaveRoom(roomIdentifier);
        participantRepo.save(participant);


        if(currentRoom.getParticipants().size() != 0) {
            chatRoomRepo.save(currentRoom);
            websocketController.sendPersonalMessage(currentRoom);
        } else {
            chatRoomRepo.deleteByChatroomIdentifier(roomIdentifier);
        }
        return ResponseEntity.status(HttpStatus.OK).body(currentRoom);
    }

    //=======================================
    //            Private methods
    //=======================================

    private Boolean isPersonCreator(String participant) {
        return participant.equals(getUsername());
    }


    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    private void addRoomsToParticipants(ChatRoom newRoom, Set<ChatParticipant> participants) {
        for (ChatParticipant participant : participants) {
            participant.getChatRooms().add(newRoom);
            participantRepo.save(participant);
        }
    }

    //=======================================
    //       Methods handling input
    //        from other services
    //=======================================

    /**
     * Receives a register object from the Authentication service when a user registers
     * @param participant containing all necessary information for this service
     */
    @ApiIgnore
    @PostMapping("/register")
    public void registerParticipant(@RequestBody ChatParticipant participant) {
        participantRepo.save(participant);

    }

    /**
     * Picture object to update the profile picture in the lobby, send by the User service
     * @param pictureDTO
     */
    @ApiIgnore
    @PostMapping("/userserviceimg")
    public void setPicture(@RequestBody UpdatePictureDTO pictureDTO) {
        ChatParticipant participant = participantRepo.findChatParticipantByEmail(pictureDTO.getEmail()).orElseThrow(RuntimeException::new);
        participant.setProfilePictureLocation(pictureDTO.getImgurl());
        participantRepo.save(participant);
    }

    /**
     * Sets the last message in a chatsession. Is send by the Chat service
     * @param chatMessage
     */
    @ApiIgnore
    @PostMapping("/handlechatmessage")
    public void handleMessageFromChat(@RequestBody Message chatMessage) {
        ChatRoom room = chatRoomRepo.getChatRoomByChatroomIdentifier(chatMessage.getChatroomIdentifier()).orElse(new ChatRoom());
        room.setLastMessage(chatMessage);
        chatRoomRepo.save(room);
        websocketController.sendPersonalMessage(room);
    }
}
