package com.example.integra_kids_mobile.ui.chatbot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotFront extends Fragment {

    private static final int REQUEST_RECORD_AUDIO = 101;

    // ── Campos da tela ────────────────────────────────────────────────
    private String sessionId = "";
    private RecyclerView recyclerChat;
    private TextInputEditText editMensagem;
    private TextInputLayout inputLayout;
    private ImageButton btnInputChatSend;
    private ImageButton btnMic;
    private List<ChatMensagem> mensagens = new ArrayList<>();
    private ChatAdapter adapter;
    private AvatarView avatarView;

    // ── Voz ───────────────────────────────────────────────────────────
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;

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
        btnMic = view.findViewById(R.id.btnMic);

        avatarView = view.findViewById(R.id.avatarView);

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

        adapter.setOnTtsRequestListener(this::falarTexto);

        inputLayout.setEnabled(false);
        btnInputChatSend.setEnabled(false);
        btnMic.setEnabled(false);

        iniciarTts();
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

        btnMic.setOnClickListener(v -> iniciarReconhecimentoDeVoz());

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
                        btnMic.setEnabled(true);

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
                        btnMic.setEnabled(true);
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
                            bot.getQuickActions(),
                            bot.getEmotion()
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
                                   List<QuickAction> actions,
                                   String emotion) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                mensagens.add(
                        new ChatMensagem(
                                texto,
                                ChatMensagem.TIPO_BOT,
                                actions,
                                emotion
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

    // ── TTS: inicialização ────────────────────────────────────────────
    private void iniciarTts() {
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "BR"));
                tts.setPitch(0.85f);
                ttsReady = true;
            }
        });
    }

    private void falarTexto(String texto) {
        if (!ttsReady) return;
        tts.stop();

        Bundle params = new Bundle();
        params.putString("voice", "pt-BR-default"); // força a voz pelo nome

        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, params, "tts_bot");
    }

    // ── STT: iniciar reconhecimento de voz ────────────────────────────
    private void iniciarReconhecimentoDeVoz() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle p) {
                btnMic.setAlpha(0.5f); // feedback visual: gravando
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float v) {}
            @Override public void onBufferReceived(byte[] b) {}
            @Override public void onEndOfSpeech() {
                btnMic.setAlpha(1f);
            }
            @Override public void onError(int error) {
                btnMic.setAlpha(1f);
                Toast.makeText(requireContext(), "Não entendi. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
            @Override public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String texto = matches.get(0);
                    editMensagem.setText(texto);
                    adicionarMensagemUsuario(texto);
                    editMensagem.setText("");
                    enviarMensagem(texto);
                }
            }
            @Override public void onPartialResults(Bundle b) {}
            @Override public void onEvent(int t, Bundle b) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    // ── STT: resultado da permissão ───────────────────────────────────
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarReconhecimentoDeVoz();
        } else {
            Toast.makeText(requireContext(), "Permissão de microfone negada.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Limpeza ───────────────────────────────────────────────────────
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}