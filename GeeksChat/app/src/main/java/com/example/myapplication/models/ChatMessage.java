package com.example.myapplication.models;

import java.util.Date;

public class ChatMessage
{
    public String senderId,receiverId,message,dateTime;
    public Date dateObject;
    public boolean isSender(String currentUserId) {
        return senderId.equals(currentUserId);
    }

    public String conversationId,conversationName,conversationImage;
    private String imageUrl;

    public boolean isRead = true;

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isImage() {
        // Check if the message has a non-null and non-empty image URL
        return imageUrl != null && !imageUrl.isEmpty();
    }


}
