package com.example.integra_kids_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.chatbot.ChatMensagem;
import com.example.integra_kids_mobile.chatbot.QuickAction;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensagemViewHolder> {

    private List<ChatMensagem> mensagens;

    public interface OnQuickActionListener {
        void onQuickAction(String value);
    }

    private OnQuickActionListener onQuickActionListener;

    public void setOnQuickActionListener(OnQuickActionListener listener) {
        this.onQuickActionListener = listener;
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
        holder.textMensagem.setText(mensagem.getTexto());

        if (holder.layoutQuickActions != null) {
            List<QuickAction> actions = mensagem.getQuickActions();

            if (actions != null && !actions.isEmpty()) {
                holder.layoutQuickActions.setVisibility(View.VISIBLE);
                holder.layoutQuickActions.removeAllViews();

                for (QuickAction action : actions) {
                    Button btn = new Button(holder.itemView.getContext());
                    btn.setText(action.getLabel());

                    // ── Visual ──────────────────────────────────────────
                    btn.setTextSize(12f);
                    btn.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text));
                    btn.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_quickaction));
                    btn.setAllCaps(false);                  // remove o ALL CAPS padrão do Button
                    btn.setPadding(32, 12, 32, 12);        // padding interno (px): left, top, right, bottom

                    // ── Layout ──────────────────────────────────────────
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
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
            } else {
                holder.layoutQuickActions.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() { return mensagens.size(); }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagem;
        FlexboxLayout layoutQuickActions;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            layoutQuickActions = itemView.findViewById(R.id.layoutQuickActions);
        }
    }
}