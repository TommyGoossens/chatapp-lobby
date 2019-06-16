package com.tommy.lobby.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveChatDTO {
    private String chatroomIdentifier, email;
}
