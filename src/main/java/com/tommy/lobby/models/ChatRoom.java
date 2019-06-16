package com.tommy.lobby.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.omg.CORBA.OBJECT_NOT_EXIST;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String chatroomIdentifier;

    @ManyToMany
    private Set<ChatParticipant> participants = new HashSet<>();

    @ElementCollection
    private Set<String> admins = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Message lastMessage;

    private boolean isGroupChat;

    private String groupName;

    private String groupDescription;

    private String groupImageLocation = "https://firebasestorage.googleapis.com/v0/b/chatapp-68f21.appspot.com/o/profile_pictures%2Fnot_yet_set_group.png?alt=media&token=b8d24aa6-0638-4c77-ae8d-7c0d4d42d4c4";

    public void updateDetails(ChatRoom updatedRoom) {
        this.admins = updatedRoom.admins;
        this.groupName = updatedRoom.groupName;
        this.groupDescription = updatedRoom.groupDescription;
        this.groupImageLocation = updatedRoom.groupImageLocation;
    }

    public void leaveRoom(String email){
        ChatParticipant participantToRemove = participants.stream().filter(p -> p.getEmail().equals(email)).findFirst().orElseThrow(OBJECT_NOT_EXIST::new);
        participants.remove(participantToRemove);

        if(admins.contains(email)){
            admins.remove(email);
            if(admins.size() == 0){
                admins.add(participants.iterator().next().getEmail());
            }
        }
    }
}
