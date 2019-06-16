package com.tommy.lobby.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class NewChatroomDTO {

    private Set<String> participants = new HashSet<>();

    private boolean groupChat;

    private String groupDescription;
}
