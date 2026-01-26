package com.example.integra_kids_mobile.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.adapter.JogadorAdapter;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.model.Jogador;
import com.example.integra_kids_mobile.API.DependenteService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerfilTrocarPlayer extends AppCompatActivity implements JogadorAdapter.OnJogadorClickListener {

    private RecyclerView recyclerJogadores;
    private JogadorAdapter jogadorAdapter;
    private List<Jogador> listaJogadores;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "USER_PREFS";
    private static final String KEY_SELECTED_PLAYER_ID = "selected_player_id";
    private static final String KEY_SELECTED_PLAYER_NAME = "selected_player_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.perfil_trocar_player);

        ReturnButton.configurar(this);

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        recyclerJogadores = findViewById(R.id.recyclerJogadores);
        Button btnCreateNewPlayer = findViewById(R.id.btnCreateNewPlayer);

        // 🔹 Buscar dependentes do usuário
        listaJogadores = new ArrayList<>();
        carregarJogadores();

        jogadorAdapter = new JogadorAdapter(listaJogadores, this);
        jogadorAdapter.setOnJogadorClickListener(this); // 🔹 listener correto

        // 🔹 GridLayout com 2 colunas
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int total = jogadorAdapter.getItemCount();
                if (total % 2 == 1 && position == total - 1) {
                    return 2; // ocupa as duas colunas → centraliza
                }
                return 1;
            }
        });

        recyclerJogadores.setLayoutManager(glm);
        recyclerJogadores.setAdapter(jogadorAdapter);

        // 🔹 Botão criar novo jogador
        btnCreateNewPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilTrocarPlayer.this, PerfilCadKid.class);
            startActivity(intent);
        });
    }

    // 🔹 Método que carrega os jogadores/dependentes
    private void carregarJogadores() {
        int userId = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        if (userId == -1) {
            Log.d("PERFIL_TROCAR", "User ID não encontrado nos SharedPreferences!");
            return;
        }

        Log.d("PERFIL_TROCAR", "Iniciando carregamento de dependentes para userId: " + userId);

        new Thread(() -> {
            List<JSONObject> dependentes = null;
            try {
                dependentes = DependenteService.getDependentesByUsuario(PerfilTrocarPlayer.this, userId);
            } catch (Exception e) {
                Log.d("PERFIL_TROCAR", "Erro ao chamar DependenteService", e);
            }

            if (dependentes == null) {
                Log.d("PERFIL_TROCAR", "Lista de dependentes retornou null");
                return;
            }

            Log.d("PERFIL_TROCAR", "Dependentes recebidos do serviço: " + dependentes.size());

            listaJogadores.clear();
            for (int i = 0; i < dependentes.size(); i++) {
                try {
                    JSONObject dep = dependentes.get(i);
                    int id = dep.getInt("id");
                    String nome = dep.getString("nome");
                    String fotoUrl = dep.optString("foto", null); // pega o campo foto, se existir
                    int icone = com.example.integra_kids_mobile.utils.AvatarMapper.getAvatarResource(fotoUrl);

                    Log.d("PERFIL_TROCAR", "Dependente " + i + ": ID=" + id + ", Nome=" + nome + ", Foto=" + fotoUrl);

                    listaJogadores.add(new Jogador(id, nome, icone));
                } catch (Exception e) {
                    Log.d("PERFIL_TROCAR", "Erro ao ler dependente " + i, e);
                }
            }

            // Atualiza RecyclerView no UI Thread
            runOnUiThread(() -> {
                if (jogadorAdapter != null) {
                    jogadorAdapter.notifyDataSetChanged();
                    Log.d("PERFIL_TROCAR", "Adapter notificado. ListaJogadores tamanho: " + listaJogadores.size());
                } else {
                    Log.d("PERFIL_TROCAR", "Adapter ainda é null!");
                }
            });
        }).start();
    }


    // 🔹 Callback quando um jogador é selecionado
    @Override
    public void onJogadorClick(Jogador jogador) {
        // 🔹 Log para ver qual jogador foi escolhido
        Log.e("PERFIL_TROCAR", "Jogador selecionado: ID=" + jogador.getId() + ", Nome=" + jogador.getNome());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SELECTED_PLAYER_ID, jogador.getId());
        editor.putString(KEY_SELECTED_PLAYER_NAME, jogador.getNome());
        String url = com.example.integra_kids_mobile.utils.AvatarMapper.getAvatarUrlFromResource(jogador.getImagemResId());
        editor.putString("selected_player_photo_url", url);
        editor.apply();

        // opcional: retornar a activity anterior
        finish();
    }

}
