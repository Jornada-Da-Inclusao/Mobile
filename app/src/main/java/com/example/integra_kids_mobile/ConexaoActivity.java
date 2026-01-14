package com.example.integra_kids_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.integra_kids_mobile.API.ApiClient;

import okhttp3.Response;

public class ConexaoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conexao);

        ImageView loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this)
                .asGif()
                .load(R.drawable.connecting) // gif
                .into(loadingGif);

        TextView tvStatus = findViewById(R.id.tvStatusTentativas);
        TextView tvFrase = findViewById(R.id.tvFrase);

        String[] frases = {
                "Preparando tudo pra você...",
                "Quase lá...",
                "Imbuindo o server com a luz do Viajante...",
                "Só mais um instante...",
                "Tocando a Song of Time para acelerar o carregamento...",
                "Estamos acordando o servidor...",
                "Checando se os fios do servidor  não foram parar em Pharloom...",
                "Checando os circuitos de redstone...",
                "Procurando a conexão dentro de uma caixa de papelão...",
                "Conectando ao servidor da NASA, só que não...",
                "Carregando o Ki da conexão...",
                "Esperar te enche de determinação...",
                "Conectando..."
        };

        new Thread(() -> {
            for (int i = 1; i <= 15; i++) {

                int tentativa = i;
                String frase = frases[i % frases.length];

                runOnUiThread(() -> {
                    tvStatus.setText("Conectando ao servidor :\nTentativa " + tentativa + " de 15...");
                    tvFrase.setText(frase);
                });

                // ===============================
                //  PRIMEIRA TENTATIVA → 3 segundos
                // ===============================
                if (i == 1) {
                    try { Thread.sleep(3000); } catch (Exception ignored) {}
                }

                boolean conectado = testarServidor();

                if (conectado) {
                    runOnUiThread(() -> {
                        tvStatus.setText("Conectado!");
                        tvFrase.setText("Tudo pronto!");
                    });

                    try { Thread.sleep(600); } catch (Exception ignored) {}

                    abrirLogin();
                    return;
                }

                // ===============================
                //  OUTRAS TENTATIVAS → 1.5 segundos
                // ===============================
                try { Thread.sleep(1500); } catch (Exception ignored) {}
            }

            // todas tentativas falharam
            runOnUiThread(() -> {
                tvStatus.setText("Não foi possível conectar :(");
                tvFrase.setText("Tente novamente mais tarde.");
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



    // ================================
    //   REDIRECIONAMENTO PARA LOGIN
    // ================================
    private void abrirLogin() {
        Intent intent = new Intent(ConexaoActivity.this, LoginCadastro.class);
        startActivity(intent);
        finish(); // impede voltar para a tela de conexão
    }
}
