package com.example.integra_kids_mobile.chatbot;

import java.util.List;

public class BotMessage {
    private String role;
    private String text;
    private List<QuickAction> quickActions; // ← atualizado
    private String emotion;

    public String getText() { return text; }
    public String getRole() { return role; }
    public String getEmotion() { return emotion; }
    public List<QuickAction> getQuickActions() { return quickActions; }
}
