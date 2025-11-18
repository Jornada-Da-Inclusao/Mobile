package com.example.integra_kids_mobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.integra_kids_mobile.API.ApiClient;

public class UsuarioConfig extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton radioSystem, radioLight, radioDark;
    private Button btnDevApi;
    private TextView textDevStatus;

    public UsuarioConfig() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.usuario_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referências dos elementos
        radioGroup = view.findViewById(R.id.radioTheme);
        radioSystem = view.findViewById(R.id.radioButton);
        radioLight = view.findViewById(R.id.radioButton2);
        radioDark = view.findViewById(R.id.radioButton3);
        textDevStatus = view.findViewById(R.id.textDevStatus);
        btnDevApi = view.findViewById(R.id.btnDevApi);

        // 🔄 Botão para testar o backend com várias tentativas
        btnDevApi.setOnClickListener(v -> {
            textDevStatus.setText("Testando conexão com o servidor...");

            new Thread(() -> {
                int maxTentativas = 15;
                int tentativa = 0;
                boolean sucesso = false;

                while (tentativa < maxTentativas && !sucesso) {
                    tentativa++;
                    final int tentativaAtual = tentativa;

                    try {
                        // Mostra tentativa atual
                        requireActivity().runOnUiThread(() ->
                                textDevStatus.setText("🔄 Tentando conectar... (" + tentativaAtual + "/" + maxTentativas + ")"));

                        // Faz o ping (pode ser /ping, /health ou / dependendo da tua API)
                        String response = ApiClient.get("/");

                        // Se não lançar exceção, conexão bem-sucedida
                        sucesso = true;
                        final String respostaFinal = response;

                        requireActivity().runOnUiThread(() ->
                                textDevStatus.setText("✅ Servidor ativo! Resposta: " + respostaFinal));

                    } catch (Exception e) {
                        // Exibe falha da tentativa atual
                        requireActivity().runOnUiThread(() ->
                                textDevStatus.setText("❌ Tentativa " + tentativaAtual + " falhou..."));

                        try {
                            // Espera 1 segundo antes da próxima tentativa
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                if (!sucesso) {
                    requireActivity().runOnUiThread(() ->
                            textDevStatus.setText("❌ Falha após " + maxTentativas + " tentativas. Servidor inativo."));
                }
            }).start();
        });

        // 🎨 Recupera e aplica o tema atual
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", requireContext().MODE_PRIVATE);
        int themeMode = prefs.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                radioDark.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                radioLight.setChecked(true);
                break;
            default:
                radioSystem.setChecked(true);
        }

        // 🌓 Listener para mudar o tema
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.radioButton2)
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            else if (checkedId == R.id.radioButton3)
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(selectedMode);
            prefs.edit().putInt("themeMode", selectedMode).apply();
        });
    }
}
