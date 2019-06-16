package com.tommy.lobby.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextMessage extends Message {
    public TextMessage() {
        super();
    }

    public TextMessage(String sender, String content){
        super();
        this.setSender(sender);
        this.setContent(content);
    }
}
