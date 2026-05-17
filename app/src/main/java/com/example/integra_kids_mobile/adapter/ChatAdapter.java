package com.example.integra_kids_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.chatbot.ChatMensagem;
import com.example.integra_kids_mobile.chatbot.QuickAction;
import com.example.integra_kids_mobile.ui.chatbot.AvatarView;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensagemViewHolder> {

    private List<ChatMensagem> mensagens;


    public interface OnQuickActionListener {
        void onQuickAction(String value);
    }

    public interface OnBotMessageRenderedListener {
        void onBotMessageRendered(ChatMensagem mensagem);
    }

    public interface OnTtsRequestListener {
        void onTtsRequest(String texto);
    }

    private OnQuickActionListener onQuickActionListener;
    private OnBotMessageRenderedListener botListener;
    private OnTtsRequestListener onTtsRequestListener;

    public void setOnQuickActionListener(OnQuickActionListener listener) {
        this.onQuickActionListener = listener;
    }

    public void setOnBotMessageRenderedListener(OnBotMessageRenderedListener listener) {
        this.botListener = listener;
    }

    public void setOnTtsRequestListener(OnTtsRequestListener listener) {
        this.onTtsRequestListener = listener;
    }

    public ChatAdapter(List<ChatMensagem> mensagens) {
        this.mensagens = mensagens;
    }

    @Override
    public int getItemViewType(int position) {
        return mensagens.get(position).getTipo();
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == ChatMensagem.TIPO_USUARIO
                ? R.layout.item_mensagem_usuario
                : R.layout.item_mensagem_bot;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        ChatMensagem mensagem = mensagens.get(position);

        if (mensagem.getTipo() == ChatMensagem.TIPO_BOT) {

            // ── Atualiza o avatar do item com a emoção da mensagem ────────
            if (holder.avatarView != null) {
                String emotion = mensagem.getEmotion();
                if (emotion != null && !emotion.isEmpty()) {
                    try {
                        holder.avatarView.setExpression(emotion);
                    } catch (IllegalArgumentException e) {
                        holder.avatarView.setExpression(AvatarView.Expression.NEUTRAL);
                    }
                } else {
                    holder.avatarView.setExpression(AvatarView.Expression.NEUTRAL);
                }
            }

            if (botListener != null && position == mensagens.size() - 1) {
                botListener.onBotMessageRendered(mensagem);
            }

            // ── Botão de ouvir a mensagem (TTS) ──────────────────────────
            if (holder.btnOuvir != null) {
                holder.btnOuvir.setOnClickListener(v -> {
                    if (onTtsRequestListener != null) {
                        onTtsRequestListener.onTtsRequest(mensagem.getTexto());
                    }
                });
            }
        }

        // ── Typewriter effect só na última mensagem do bot ───────────────
        if (mensagem.getTipo() == ChatMensagem.TIPO_BOT && position == mensagens.size() - 1) {

            // Esconde as quickActions até o texto terminar de "digitar"
            if (holder.layoutQuickActions != null) {
                holder.layoutQuickActions.setVisibility(View.GONE);
            }

            animarTextoDigitando(holder.textMensagem, mensagem.getTexto(), () -> {
                // Exibe as quickActions só depois que o texto terminar
                if (holder.layoutQuickActions != null) {
                    List<QuickAction> actions = mensagem.getQuickActions();
                    if (actions != null && !actions.isEmpty()) {
                        popularQuickActions(holder, actions);
                        holder.layoutQuickActions.setVisibility(View.VISIBLE);
                    }
                }
            });

        } else {
            // Mensagens antigas e mensagens do usuário aparecem normais
            holder.textMensagem.setText(mensagem.getTexto());

            // ── QuickActions para mensagens antigas ──────────────────────
            if (holder.layoutQuickActions != null) {
                List<QuickAction> actions = mensagem.getQuickActions();
                if (actions != null && !actions.isEmpty()) {
                    popularQuickActions(holder, actions);
                    holder.layoutQuickActions.setVisibility(View.VISIBLE);
                } else {
                    holder.layoutQuickActions.setVisibility(View.GONE);
                }
            }
        }
    }

    // ── Monta os botões de quickActions no FlexboxLayout ─────────────────
    private void popularQuickActions(@NonNull MensagemViewHolder holder, List<QuickAction> actions) {
        holder.layoutQuickActions.removeAllViews();

        for (QuickAction action : actions) {
            Button btn = new Button(holder.itemView.getContext());
            btn.setText(action.getLabel());

            // ── Visual ───────────────────────────────────────────────────
            btn.setTextSize(12f);
            btn.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text));
            btn.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_quickaction));
            btn.setAllCaps(false);
            btn.setPadding(32, 12, 32, 12);

            // ── Layout: FlexboxLayout.LayoutParams para wrap correto ─────
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 6, 12, 6);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                if (onQuickActionListener != null) {
                    onQuickActionListener.onQuickAction(action.getValue());
                }
            });

            holder.layoutQuickActions.addView(btn);
        }
    }

    // ── Typewriter effect ─────────────────────────────────────────────────
    private void animarTextoDigitando(TextView textView, String textoCompleto, Runnable aoTerminar) {
        textView.setText("");
        final int[] index = {0};

        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= textoCompleto.length()) {
                    textView.setText(textoCompleto.substring(0, index[0]));
                    index[0]++;
                    if (onScrollRequestListener != null) {
                        onScrollRequestListener.onScrollRequest();
                    }
                    handler.postDelayed(this, 18); // velocidade: menor = mais rápido
                } else {
                    if (aoTerminar != null) aoTerminar.run();
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagem;
        FlexboxLayout layoutQuickActions;
        AvatarView avatarView;
        android.widget.ImageButton btnOuvir;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            layoutQuickActions = itemView.findViewById(R.id.layoutQuickActions);
            avatarView = itemView.findViewById(R.id.avatarView);
            btnOuvir = itemView.findViewById(R.id.btnOuvir); // só existe no item_mensagem_bot
        }
    }

    // Adiciona a interface e o setter (junto com o OnQuickActionListener)
    public interface OnScrollRequestListener {
        void onScrollRequest();
    }

    private OnScrollRequestListener onScrollRequestListener;

    public void setOnScrollRequestListener(OnScrollRequestListener listener) {
        this.onScrollRequestListener = listener;
    }
}