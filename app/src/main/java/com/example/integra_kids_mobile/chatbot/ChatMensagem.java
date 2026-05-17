package com.example.integra_kids_mobile.chatbot;

import java.util.List;

public class ChatMensagem {
    public static final int TIPO_USUARIO = 0;
    public static final int TIPO_BOT = 1;

    private String texto;
    private int tipo;
    private List<QuickAction> quickActions;
    private String emotion;

    public ChatMensagem(String texto, int tipo) {
        this.texto = texto;
        this.tipo = tipo;
    }

    public ChatMensagem(String texto, int tipo, List<QuickAction> quickActions) {
        this.texto = texto;
        this.tipo = tipo;
        this.quickActions = quickActions;
    }

    public ChatMensagem(String texto, int tipo, List<QuickAction> quickActions, String emotion) {
        this.texto = texto;
        this.tipo = tipo;
        this.quickActions = quickActions;
        this.emotion = emotion;
    }

    public String getTexto() { return texto; }
    public int getTipo() { return tipo; }
    public List<QuickAction> getQuickActions() { return quickActions; }
    public String getEmotion() { return emotion; }
}