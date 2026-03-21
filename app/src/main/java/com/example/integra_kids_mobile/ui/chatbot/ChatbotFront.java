package com.example.integra_kids_mobile.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.integra_kids_mobile.R;

public class ChatbotFront extends Fragment {

    public ChatbotFront() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chatbot_front, container, false);

        // Edge-to-edge (adaptado para Fragment)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

            View navBar = getActivity() != null ? getActivity().findViewById(R.id.nav_view) : null;

            int bottomPadding;

            if (isKeyboardVisible) {
                // 1. TECLADO ATIVO:
                if (navBar != null) navBar.setVisibility(View.GONE);
                // O padding inferior passa a ser exatamente a altura do teclado
                bottomPadding = ime.bottom;
            } else {
                // 2. TECLADO INATIVO:
                if (navBar != null) {
                    navBar.setVisibility(View.VISIBLE);

                    // Aqui pegamos a altura REAL da Navbar + barras do sistema (gestos/botões)
                    // Usamos post() ou getHeight() se ela já estiver renderizada
                    int navHeight = navBar.getHeight();
                    bottomPadding = navHeight > 0 ? navHeight : systemBars.bottom + 140;
                } else {
                    bottomPadding = systemBars.bottom;
                }
            }

            // Aplica o padding dinâmico no container do Fragment
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);

            return insets;
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // EdgeToEdge.enable precisa de Activity
        if (getActivity() != null) {
            EdgeToEdge.enable(getActivity());
        }
    }


}