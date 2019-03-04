package com.example.xiaobaiz.chat;


/**
 * Created by Technovibe on 17-04-2015.
 */
public class ChatMessage {
    private String username;
    private boolean isMe;
    private String message;
    private String dateTime;
    ChatMessage(String username,String message,String dateTime,boolean isMe){
        this.username = username;
        this.message = message;
        this.dateTime = dateTime;
        this.isMe = isMe;
    }
    public boolean getIsme() {
        return isMe;
    }
    public String getUsername(){
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return dateTime;
    }

}

