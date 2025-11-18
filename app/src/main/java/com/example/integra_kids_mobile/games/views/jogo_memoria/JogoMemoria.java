package com.example.integra_kids_mobile.games.views.jogo_memoria;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.components.jogo_da_memoria.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JogoMemoria extends AppCompatActivity {
    private long id = 1;
    private GridLayout gridLayout;
    private InfoJogos infoJogos = new InfoJogos(id, 0); // hardcoded
    Timer timer;

    private List<Card> cards;
    private Card firstCard = null;
    private Card secondCard = null;
    private boolean isProcessing = false;

    private int matchedPairs = 0;
    private final int TOTAL_PAIRS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jogo_memoria);

        initializeViews();
        initializeGame();
        infoJogos.comecarJogo();

        timer = new Timer(this);
        FrameLayout rootLayout = findViewById(R.id.timer);
        rootLayout.addView(timer);
        timer.startTimer();
    }

    private void initializeViews() {
        gridLayout = findViewById(R.id.gridLayout);
    }

    private void initializeGame() {
        cards = new ArrayList<>();
        gridLayout.removeAllViews();

        // Create 8 cards (4 pairs)
        int[] cardValues = {1, 1, 2, 2, 3, 3, 4, 4};

        // Shuffle the cards
        List<Integer> shuffledValues = new ArrayList<>();
        for (int value : cardValues) {
            shuffledValues.add(value);
        }
        Collections.shuffle(shuffledValues);

        // Create card buttons
        for (int i = 0; i < 8; i++) {
            Card card = new Card(this, shuffledValues.get(i), i);
            card.setOnClickListener(v -> onCardClicked(card));
            cards.add(card);
            gridLayout.addView(card);
        }
    }

    private void onCardClicked(Card card) {
        if (isProcessing || card.isMatched() || card.isFaceUp()) {
            return;
        }

        card.flip();

        if (firstCard == null) {
            firstCard = card;
        } else if (secondCard == null) {
            secondCard = card;
            isProcessing = true;
            infoJogos.setTentativas(infoJogos.getTentativas() + 1);

            checkForMatch();
        }
    }

    private void checkForMatch() {
        if (firstCard.getValue() == secondCard.getValue()) {
            // Match found!
            infoJogos.setAcertos(infoJogos.getAcertos() + 1);
            matchedPairs++;
            firstCard.setMatched(true);
            secondCard.setMatched(true);

            Toast.makeText(this, "Match found! 🎉", Toast.LENGTH_SHORT).show();

            firstCard = null;
            secondCard = null;
            isProcessing = false;

            checkGameComplete();

        } else {
            // No match
            infoJogos.setErros(infoJogos.getErros() + 1);

            // Flip cards back after delay
            new Handler().postDelayed(() -> {
                if (firstCard != null) firstCard.flip();
                if (secondCard != null) secondCard.flip();

                firstCard = null;
                secondCard = null;
                isProcessing = false;
            }, 1000);
        }
    }

    private void checkGameComplete() {
        if (matchedPairs == TOTAL_PAIRS) {
            infoJogos.terminarJogo();
            timer.stopTimer();

            try {
                GameService.cadastrarInfo(this, infoJogos.getDependenteId(), infoJogos.getInfoJogos_id_fk(), infoJogos.getAcertos(), infoJogos.getErros(), infoJogos.getTempoTotal());
            } catch (Exception e) {
                throw new RuntimeException(e);
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setPositiveButton("Voltar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setTitle("Missão concluída!")
                    .setMessage("Parabéns! Você completou o jogo.");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
