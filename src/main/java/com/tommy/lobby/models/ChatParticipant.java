package com.tommy.lobby.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.omg.CORBA.OBJECT_NOT_EXIST;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @NotBlank(message = "Email may not be null")
    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    private String profilePictureLocation = "https://firebasestorage.googleapis.com/v0/b/chatapp-68f21.appspot.com/o/profile_pictures%2Fnot_yet_set.png?alt=media&token=0ee8fca4-196d-4fc7-bbea-f0e366a21a39";

    @ManyToMany
    @JsonIgnore
    private Set<ChatRoom> chatRooms = new HashSet<>();


    public void leaveRoom(String roomIdentifier) {
        ChatRoom room = chatRooms.stream().filter(r -> r.getChatroomIdentifier().equals(roomIdentifier)).findFirst().orElseThrow(OBJECT_NOT_EXIST::new);
        chatRooms.remove(room);
    }
}
