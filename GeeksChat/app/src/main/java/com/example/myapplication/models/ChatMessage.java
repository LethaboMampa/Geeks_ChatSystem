package com.example.myapplication.models;

import java.util.Date;

public class ChatMessage
{
    public String senderId,receiverId,message,dateTime;
    public Date dateObject;
    public boolean isSender(String currentUserId) {
        return senderId.equals(currentUserId);
    }

}
