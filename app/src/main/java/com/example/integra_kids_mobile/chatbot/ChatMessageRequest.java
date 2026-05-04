package com.example.integra_kids_mobile.chatbot;

public class ChatMessageRequest {
    private String sessionId;
    private String text;

    public ChatMessageRequest(String sessionId, String text) {
        this.sessionId = sessionId;
        this.text = text;
    }
}
