package com.example.integra_kids_mobile;

import static android.view.View.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.integra_kids_mobile.API.ApiClient;
import com.example.integra_kids_mobile.chatbot.ChatStartResponse;
import com.example.integra_kids_mobile.chatbot.RetrofitClient;
import com.example.integra_kids_mobile.helper.AccessibilityHelper;

import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ConexaoActivity extends AppCompatActivity {

    String[] frases = {
            "Preparando tudo pra você...",
            "Quase lá...",
            "Imbuindo o server com a luz do Traveler...",
            "Só mais um instante...",
            "Tocando a Song of Time para acelerar o carregamento...",
            "Estamos acordando o servidor...",
            "Checando se os fios do servidor não foram parar em Pharloom...",
            "Checando os circuitos de redstone...",
            "Procurando a conexão dentro de uma caixa de papelão...",
            "Conectando ao servidor da NASA (só que não)...",
            "Elevando o Ki da conexão...",
            "Esperar te enche de determinação...",
            "Conectando..."
    };

    TextView tvStatus;
    TextView tvFrase;
    ProgressBar progressBar;
    Button btnReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conexao);

        AccessibilityHelper.applyColorblindFilter(this);
        AccessibilityHelper.applyFontScale(this);

        tvStatus = findViewById(R.id.tvStatusTentativas);
        tvFrase = findViewById(R.id.tvFrase);
        progressBar = findViewById(R.id.progressConnect);
        btnReload = findViewById(R.id.btnReloadConnection);

        ImageView loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this)
                .asGif()
                .load(R.drawable.connecting) // gif
                .into(loadingGif);

        btnReload.setVisibility(GONE);

        btnReload.setOnClickListener( v -> {
            iniciarTentativas();
            progressBar.setVisibility(VISIBLE);
            btnReload.setVisibility(GONE);
        });

        iniciarTentativas();

    }

    private void iniciarTentativas() {
        new Thread(() -> {
            java.util.Random random = new java.util.Random();
            for (int i = 1; i <= 15; i++) {

                int tentativa = i;
                String frase = frases[random.nextInt(frases.length)];

                runOnUiThread(() -> {
                    tvStatus.setText("Conectando ao servidor :\nTentativa " + tentativa + " de 15...");
                    tvFrase.setText(frase);
                });

                // ===============================
                //  PRIMEIRA TENTATIVA → 3 segundos
                // ===============================
                if (i == 1) {
                    acordarChatbot();
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ignored) {
                    }
                }

                boolean conectado = testarServidor();

                if (conectado) {
                    runOnUiThread(() -> {
                        tvStatus.setText("Conectado!");
                        tvFrase.setText("Tudo pronto!");
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(100);
                    });

                    try {
                        Thread.sleep(600);
                    } catch (Exception ignored) {
                    }

                    abrirLogin();
                    return;
                }

                // ===============================
                //  OUTRAS TENTATIVAS → 1.5 segundos
                // ===============================
                try {
                    Thread.sleep(1500);
                } catch (Exception ignored) {
                }
            }

            // todas tentativas falharam
            runOnUiThread(() -> {
                tvStatus.setText("Não foi possível conectar :(");
                tvFrase.setText("Tente novamente mais tarde.");
                progressBar.setVisibility(GONE);
                btnReload.setVisibility(VISIBLE);
            });

        }).start();
    }

    private boolean testarServidor() {
        try {
            Response response = ApiClient.get(this, "/");

            // Se o servidor respondeu QUALQUER COISA, já está acordado
            return response != null && response.body() != null;

        } catch (Exception e) {
            // Sem resposta = servidor offline
            return false;
        }
    }

    private void acordarChatbot() {

        android.util.Log.d("CHATBOT_WAKE", "Iniciando wake-up do chatbot...");

        RetrofitClient
                .getInstance()
                .startSession()
                .enqueue(new Callback<ChatStartResponse>() {

                    @Override
                    public void onResponse(
                            Call<ChatStartResponse> call,
                            retrofit2.Response<ChatStartResponse> response) {

                        if (response.isSuccessful()) {
                            android.util.Log.d(
                                    "CHATBOT_WAKE",
                                    "Chatbot acordado com sucesso! Code: "
                                            + response.code()
                            );
                        } else {
                            android.util.Log.w(
                                    "CHATBOT_WAKE",
                                    "Chatbot respondeu, mas com erro. Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ChatStartResponse> call,
                            Throwable t) {

                        android.util.Log.e(
                                "CHATBOT_WAKE",
                                "Falha ao acordar chatbot: " + t.getMessage()
                        );
                    }
                });
    }

    // ================================
    //   REDIRECIONAMENTO PARA LOGIN
    // ================================
    private void abrirLogin() {
        Intent intent = new Intent(ConexaoActivity.this, LoginCadastro.class);
        startActivity(intent);
        finish(); // impede voltar para a tela de conexão
    }
}
