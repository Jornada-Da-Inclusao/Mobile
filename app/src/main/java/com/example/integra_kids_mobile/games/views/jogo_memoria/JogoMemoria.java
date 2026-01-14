package com.example.integra_kids_mobile.games.views.jogo_memoria;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.games.views.jogo_memoria.KeyView;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.model.CardModel;
import com.example.integra_kids_mobile.music.BackgroundMusics;
import com.example.integra_kids_mobile.music.SoundEffects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JogoMemoria extends AppCompatActivity {

    private final long idJogoMemoria = 1;
    private InfoJogos infoJogos;
    private int dependenteId;
    private Timer timer;
    private GridLayout gridLayout;

    private List<CardModel> cards = new ArrayList<>();
    private List<Integer> cardsChosenId = new ArrayList<>();
    private List<Integer> cardsWon = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jogo_memoria);

        ReturnButton.configurar(this);

        dependenteId = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                .getInt("selected_player_id", -1);
        infoJogos = new InfoJogos(idJogoMemoria, dependenteId);

        // Timer
        timer = new Timer(this);
        gridLayout = findViewById(R.id.memoria_grid);
        findViewById(R.id.timer).setOnClickListener(v -> {}); // apenas placeholder
        ((ViewGroup) findViewById(R.id.timer)).addView(timer);
        timer.startTimer();

        carregarCartas();
        infoJogos.comecarJogo();

        SoundEffects.init(this);
        BackgroundMusics.start(this, R.raw.music1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa a música quando o app for para segundo plano
        BackgroundMusics.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retoma a música quando o usuário voltar para o app
        BackgroundMusics.resume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Isso garante que, ao sair desta tela (ou fechar o jogo), a música pare totalmente
        BackgroundMusics.stop();
    }

    private void carregarCartas() {
        // Lista completa de imagens
        int[] imgs = {
                R.drawable.pequena,
                R.drawable.pequena2,
                R.drawable.pequena3,
                R.drawable.pequena4,
                R.drawable.pequena5,
                R.drawable.pequena6
        };

        cards.clear();

        // Seleciona só os 4 primeiros ícones para os pares
        for (int i = 0; i < 4; i++) {
            CardModel carta1 = new CardModel("imagem" + (i+1), imgs[i]);
            CardModel carta2 = new CardModel("imagem" + (i+1), imgs[i]); // par
            cards.add(carta1);
            cards.add(carta2);
        }

        // Embaralha os 8 cartões
        Collections.shuffle(cards);

        // Cria os KeyViews
        gridLayout.removeAllViews(); // limpa antes de adicionar
        for (int i = 0; i < cards.size(); i++) {
            final int index = i;
            KeyView key = new KeyView(this);
            key.setDrawable(cards.get(i).imageRes);
            key.hide();

            int size = 200; // ou calcule dinamicamente
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(56, 16, 16, 16);

            key.setLayoutParams(params);
            key.setOnClickListener(v -> flipCard(index));
            gridLayout.addView(key);
        }
    }



    private void flipCard(int index) {
        if (cardsChosenId.contains(index) || cardsWon.contains(index)) return;

        SoundEffects.tocarClique();
        cardsChosenId.add(index);
        KeyView kv = (KeyView) gridLayout.getChildAt(index);
        kv.reveal();

        if (cardsChosenId.size() == 2) {
            new Handler().postDelayed(this::checkForMatch, 600);
        }
    }

    private void checkForMatch() {
        int id1 = cardsChosenId.get(0);
        int id2 = cardsChosenId.get(1);
        infoJogos.setTentativas(infoJogos.getTentativas() + 1);

        CardModel c1 = cards.get(id1);
        CardModel c2 = cards.get(id2);

        if (c1.name.equals(c2.name)) {
            cardsWon.add(id1);
            cardsWon.add(id2);
            SoundEffects.tocarAcerto();
            infoJogos.setAcertos(infoJogos.getAcertos() + 1);
        } else {
            SoundEffects.tocarErro();
            infoJogos.setErros(infoJogos.getErros() + 1);
            ((KeyView) gridLayout.getChildAt(id1)).hide();
            ((KeyView) gridLayout.getChildAt(id2)).hide();
        }

        cardsChosenId.clear();

        if (cardsWon.size() == cards.size()) {
            finalizarJogo();
        }
    }

    private void finalizarJogo() {
        infoJogos.terminarJogo();
        timer.stopTimer();

        BackgroundMusics.stop();
        SoundEffects.tocarSucesso();
        registrarResultadoPartida();

        new AlertDialog.Builder(this)
                .setTitle("Missão concluída!")
                .setMessage("Você completou o jogo!")
                .setPositiveButton("OK", (DialogInterface d, int w) -> finish())
                .show();
    }

    private void registrarResultadoPartida() {
        new Thread(() -> {
            try {
                JSONObject resp = GameService.registrarResultado(
                        this,
                        dependenteId,
                        idJogoMemoria,
                        infoJogos.getAcertos(),
                        infoJogos.getErros(),
                        timer.getTime()
                );
                runOnUiThread(() ->
                        Toast.makeText(this, "Resultado registrado!", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao registrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
