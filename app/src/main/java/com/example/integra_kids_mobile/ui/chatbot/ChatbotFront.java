package com.example.integra_kids_mobile.ui.chatbot;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.adapter.ChatAdapter;
import com.example.integra_kids_mobile.chatbot.BotMessage;
import com.example.integra_kids_mobile.chatbot.ChatMessageRequest;
import com.example.integra_kids_mobile.chatbot.ChatStartResponse;
import com.example.integra_kids_mobile.chatbot.QuickAction;
import com.example.integra_kids_mobile.chatbot.RetrofitClient;
import com.example.integra_kids_mobile.chatbot.ChatMensagem;
import com.example.integra_kids_mobile.helper.AccessibilityHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotFront extends Fragment {

    // ✅ Campos declarados no escopo da classe (não dentro de métodos!)
    private String sessionId = "";
    private RecyclerView recyclerChat;
    private TextInputEditText editMensagem;
    private TextInputLayout inputLayout;
    private ImageButton btnInputChatSend;
    private List<ChatMensagem> mensagens = new ArrayList<>();
    private ChatAdapter adapter;

    public ChatbotFront() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chatbot_front, container, false);
        AccessibilityHelper.applyFontScale(view);

        recyclerChat = view.findViewById(R.id.recyclerChat);
        editMensagem = view.findViewById(R.id.editMensagem);
        inputLayout = view.findViewById(R.id.inputLayout);
        btnInputChatSend = view.findViewById(R.id.btnInputChatSend);

        adapter = new ChatAdapter(mensagens);
        recyclerChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerChat.setAdapter(adapter);
        adapter.setOnScrollRequestListener(() -> {
            recyclerChat.scrollToPosition(mensagens.size() - 1);
        });

        adapter.setOnQuickActionListener(value -> {
            adicionarMensagemUsuario(value);
            enviarMensagem(value);
        });

        inputLayout.setEnabled(false);
        btnInputChatSend.setEnabled(false);

        iniciarSessao();

        btnInputChatSend.setOnClickListener(v -> {
            String texto = editMensagem.getText() != null
                    ? editMensagem.getText().toString().trim()
                    : "";
            if (!texto.isEmpty()) {
                adicionarMensagemUsuario(texto);
                editMensagem.setText("");
                esconderTeclado();
                enviarMensagem(texto);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            return insets;
        });

        return view;
    }

    private void iniciarSessao() {
        RetrofitClient.getInstance().startSession().enqueue(new Callback<ChatStartResponse>() {
            @Override
            public void onResponse(Call<ChatStartResponse> call, Response<ChatStartResponse> response) {
                Log.d("Chatbot", "Código de resposta: " + response.code());

                if (!isAdded() || getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    sessionId = response.body().getSessionId();
                    String boasVindas = response.body().getWelcomeMessage() != null
                            ? response.body().getWelcomeMessage().getText()
                            : "Olá! Como posso ajudar?";

                    Log.d("Chatbot", "SessionId: " + sessionId);
                    Log.d("Chatbot", "Boas vindas: " + boasVindas);

                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;

                        inputLayout.setEnabled(true);
                        btnInputChatSend.setEnabled(true);

                        List<QuickAction> actions = response.body().getWelcomeMessage().getQuickActions();
                        mensagens.add(new ChatMensagem(boasVindas, ChatMensagem.TIPO_BOT, actions));
                        adapter.notifyItemInserted(mensagens.size() - 1);
                        recyclerChat.scrollToPosition(mensagens.size() - 1);
                    });

                } else {
                    Log.e("Chatbot", "Resposta inválida: " + response.code());
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        inputLayout.setEnabled(true);
                        btnInputChatSend.setEnabled(true);
                    });
                }
            }

            @Override
            public void onFailure(Call<ChatStartResponse> call, Throwable t) {
                Log.e("Chatbot", "FALHA na sessão: " + t.getMessage());
                if (!isAdded() || getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    inputLayout.setEnabled(true);
                    btnInputChatSend.setEnabled(true);
                });
            }
        });
    }

    private void enviarMensagem(String texto) {
        btnInputChatSend.setEnabled(false);

        ChatMessageRequest request = new ChatMessageRequest(sessionId, texto);

        RetrofitClient.getInstance().sendMessage(request).enqueue(new Callback<BotMessage>() {
            @Override
            public void onResponse(Call<BotMessage> call, Response<BotMessage> response) {
                if (response.isSuccessful() && response.body() != null) {

                    BotMessage bot = response.body();

                    exibirMensagemBot(
                            bot.getText(),
                            bot.getQuickActions()
                    );
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> btnInputChatSend.setEnabled(true));
                }
            }

            @Override
            public void onFailure(Call<BotMessage> call, Throwable t) {
                Log.e("Chatbot", "Erro: " + t.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> btnInputChatSend.setEnabled(true));
                }
            }
        });
    }

    private void adicionarMensagemUsuario(String texto) {
        mensagens.add(new ChatMensagem(texto, ChatMensagem.TIPO_USUARIO));
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerChat.scrollToPosition(mensagens.size() - 1);
    }

    private void exibirMensagemBot(String texto,
                                   List<QuickAction> actions) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                mensagens.add(
                        new ChatMensagem(
                                texto,
                                ChatMensagem.TIPO_BOT,
                                actions
                        )
                );

                adapter.notifyItemInserted(mensagens.size() - 1);
                recyclerChat.scrollToPosition(mensagens.size() - 1);
            });
        }
    }

    private void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(editMensagem.getWindowToken(), 0);
    }
}