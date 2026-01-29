package com.example.integra_kids_mobile.games.views.jogo_numeros;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.common.GameResultDialog;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.games.components.KeyView;
import com.example.integra_kids_mobile.games.components.KeyViewStateEnum;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.games.components.jogo_numeros.NumeroView;
import com.example.integra_kids_mobile.games.views.jogo_memoria.JogoMemoria;
import com.example.integra_kids_mobile.music.BackgroundMusics;
import com.example.integra_kids_mobile.music.SoundEffects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JogoNumeros extends AppCompatActivity {
    private final long id = 2;
    private final InfoJogos infoJogos = new InfoJogos(this.id, 0); // hardcoded
    private TextView[] numberBoxes = new TextView[10];
    private int size = 0;
    private int numberToBePlaced = 0;
    private int placedKeyViews = 0;
    private int selectedNumberBoxIdx = -1;
    final String[] colors = { "#E9E9E9", "#B8B6B6" }; // branco
    int currentColor = 0;
    private int dependenteId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jogo_numeros);
        dependenteId = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                .getInt("selected_player_id", -1);

        ReturnButton.configurar(this);

        Timer timer = new Timer(this);
        FrameLayout rootLayout = findViewById(R.id.timer);
        rootLayout.addView(timer);
        timer.setTimerListener(new Timer.TimerListener() {
            @Override
            public void onTimeFinished() {
                // Chamada para a sua classe utilitária de Dialogs
                // Usamos false porque o tempo acabou (derrota)
                int corTema = ContextCompat.getColor(JogoNumeros.this, R.color.yellow);
                GameResultDialog.mostrarModalFeedback(JogoNumeros.this, false, corTema);
            }
        });

        timer.startTimer();
        SoundEffects.init(this);
        BackgroundMusics.start(this, R.raw.music2);

        GridLayout gridLayout = findViewById(R.id.numeros_grid);

        for (int i = 0; i <= 9; i++) {
            // Crie o numero
            TextView numberView = new TextView(this);
            numberView.setId(i);
            numberView.setText(Integer.toString(i));
            numberView.setTextColor(Color.BLACK);
            numberView.setTextSize(24);
            numberView.setGravity(Gravity.CENTER);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(10);
            drawable.setColor(Color.parseColor(this.colors[currentColor]));
            drawable.setStroke(8, Color.BLACK);
            numberView.setBackground(drawable);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(85, 85);
            params.gravity = Gravity.TOP | Gravity.END;
            params.setMargins(0, 40, 40, 0);
            numberView.setLayoutParams(params);

            // Coloque o numero no grid
            final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            gridLayout.addView(numberView);
            numberBoxes[size++] = numberView;

            GridLayout placedNumbersContainer = findViewById(R.id.placed_numeros);

            // Coloque a cor no seu respectivo container
            placedNumbersContainer.setOnClickListener(v -> {
                TextView n = numberBoxes[numberToBePlaced];
                infoJogos.setTentativas(infoJogos.getTentativas() + 1);

                if (this.selectedNumberBoxIdx == numberToBePlaced) {
                    SoundEffects.tocarAcerto();
                    infoJogos.setAcertos(infoJogos.getAcertos() + 1);
                    placedKeyViews += 1;

                    final ViewGroup oldParent = (ViewGroup) n.getParent();
                    final GridLayout newParent = (GridLayout) v;

                    // Animação entre ViewGroups. Importante ser uma Transition e não qualquer
                    // animação normal (seja com ValueAnimator ou animação de View), caso contrário
                    // o objeto é cortado se sair do parent
                    Transition move = new ChangeTransform().addTarget(n).setDuration(300);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.numeros_root), move);
                    oldParent.removeView(n);
                    newParent.addView(n);
                    numberToBePlaced++;

                    n.setFocusable(false);

                    if (placedKeyViews == size) {
                        infoJogos.terminarJogo();
                        timer.stopTimer();

                        BackgroundMusics.stop();
                        SoundEffects.tocarSucesso();
                        registrarResultadoPartida(
                                dependenteId,
                                this.id,     // ou this.id, depende do que sua API espera
                                infoJogos.getAcertos(),
                                infoJogos.getErros(),
                                timer.getTime()
                        );

                        int corTema = ContextCompat.getColor(JogoNumeros.this, R.color.yellow);
                        GameResultDialog.mostrarModalFeedback(this, true, corTema);
                    }
                } else {
                    SoundEffects.tocarErro();
                    infoJogos.setErros(infoJogos.getErros() + 1);
                }
            });

            // Faça highlight no círculo caso selecionado
            numberView.setOnClickListener(v -> {
                final TextView kv = (TextView) v;
                if (! kv.isFocusable()) {
                    return;
                }

                SoundEffects.tocarClique();
                GradientDrawable _drawable;

                // Restaure a cor do colorbox se já tocado
                if (this.selectedNumberBoxIdx == kv.getId()) {
                    _drawable = (GradientDrawable) kv.getBackground();
                    _drawable.setColor(Color.parseColor(colors[currentColor++ % 2]));
                    return;
                }

                // Restaure a cor do colorbox anterior
                if (this.selectedNumberBoxIdx != -1) {
                    _drawable = (GradientDrawable) numberBoxes[this.selectedNumberBoxIdx].getBackground();
                    _drawable.setColor(Color.parseColor(colors[0]));
                }

                this.selectedNumberBoxIdx = kv.getId();
                _drawable = (GradientDrawable) numberBoxes[this.selectedNumberBoxIdx].getBackground();
                _drawable.setColor(Color.parseColor(colors[1]));
            });
        }

        infoJogos.comecarJogo();
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

    private void registrarResultadoPartida(
            long dependenteId,
            long infoJogoId,
            int acertos,
            int erros,
            int tempo
    ) {

        new Thread(() -> {
            try {
                JSONObject resp = GameService.registrarResultado(
                        this,
                        dependenteId,
                        infoJogoId,
                        acertos,
                        erros,
                        tempo
                );

                runOnUiThread(() -> {
                    Toast.makeText(this, "Resultado registrado!", Toast.LENGTH_SHORT).show();
                    // Se quiser finalizar a Activity, descomente:
                    // finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao registrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
