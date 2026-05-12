package com.example.integra_kids_mobile.games.views.jogo_vogais;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
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
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.games.views.jogo_memoria.JogoMemoria;
import com.example.integra_kids_mobile.helper.AccessibilityHelper;
import com.example.integra_kids_mobile.music.BackgroundMusics;
import com.example.integra_kids_mobile.music.SoundEffects;

import org.json.JSONObject;

public class JogoVogais extends AppCompatActivity {
    private final long id = 3;
    private final InfoJogos infoJogos = new InfoJogos(this.id, 0); // hardcoded
    private TextView[] letterBoxes = new TextView[26];
    private int size = 0;
    private int letterToBePlaced = 0;
    private char[] vogais = {'a', 'e', 'i', 'o', 'u'};
    private int placedKeyViews = 0;
    private int selectedLetterBoxIdx = -1;
    final String[] colors = { "#E9E9E9", "#B8B6B6" }; // branco
    int currentColor = 0;
    private int dependenteId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jogo_vogais);

        AccessibilityHelper.applyColorblindFilter(this);
        AccessibilityHelper.applyFontScale(this);

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
                int corTema = ContextCompat.getColor(JogoVogais.this, R.color.blue);
                GameResultDialog.mostrarModalFeedback(JogoVogais.this, false, corTema);
            }
        });

        timer.startTimer();
        final GridLayout gridLayout = findViewById(R.id.vogais_grid);

        // ========== CRIAÇÃO DAS LETRAS ==========
        for (int i = 'a'; i <= 'z'; i++) {

            TextView letterView = new TextView(this);
            letterView.setId(i - 'a');
            letterView.setText(Character.toString(Character.toUpperCase((char) i)));
            letterView.setTextColor(Color.BLACK);
            letterView.setTextSize(24);
            letterView.setGravity(Gravity.CENTER);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(10);
            drawable.setColor(Color.parseColor(this.colors[currentColor]));
            drawable.setStroke(8, Color.BLACK);
            letterView.setBackground(drawable);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(85, 85);
            params.gravity = Gravity.TOP | Gravity.END;
            params.setMargins(0, 40, 40, 0);
            letterView.setLayoutParams(params);

            gridLayout.addView(letterView);
            letterBoxes[size++] = letterView;

            // ========= CLICK EM UMA LETRA =========
            letterView.setOnClickListener(v -> {
                SoundEffects.tocarClique();
                final TextView kv = (TextView) v;

                if (!kv.isFocusable()) return;

                GradientDrawable _drawable;

                // Se clicar novamente, alterna cor
                if (this.selectedLetterBoxIdx == kv.getId()) {
                    _drawable = (GradientDrawable) kv.getBackground();
                    _drawable.setColor(Color.parseColor(colors[currentColor++ % 2]));
                    return;
                }

                // Restaura o anterior
                if (this.selectedLetterBoxIdx != -1) {
                    _drawable = (GradientDrawable) letterBoxes[this.selectedLetterBoxIdx].getBackground();
                    _drawable.setColor(Color.parseColor(colors[0]));
                }

                // Seleciona novo
                this.selectedLetterBoxIdx = kv.getId();
                _drawable = (GradientDrawable) kv.getBackground();
                _drawable.setColor(Color.parseColor(colors[1]));
            });
        }

        // =========================================
        //        LISTENER DO placed_vogais
        // (AGORA FORA DO FOR — DO JEITO CORRETO)
        // =========================================
        GridLayout placedNumbersContainer = findViewById(R.id.placed_vogais);

        placedNumbersContainer.setOnClickListener(v -> {
            TextView n = letterBoxes[vogais[letterToBePlaced] - 'a'];
            infoJogos.setTentativas(infoJogos.getTentativas() + 1);

            if (this.selectedLetterBoxIdx == vogais[letterToBePlaced] - 'a') {

                SoundEffects.tocarAcerto();
                infoJogos.setAcertos(infoJogos.getAcertos() + 1);
                placedKeyViews++;

                final ViewGroup oldParent = (ViewGroup) n.getParent();
                final GridLayout newParent = (GridLayout) v;

                Transition move = new ChangeTransform().addTarget(n).setDuration(300);
                TransitionManager.beginDelayedTransition(findViewById(R.id.vogais_root), move);

                oldParent.removeView(n);
                newParent.addView(n);

                letterToBePlaced++;
                n.setFocusable(false);

                // JOGO FINALIZADO
                if (placedKeyViews == vogais.length) {

                    infoJogos.terminarJogo();
                    timer.stopTimer();

                    BackgroundMusics.stop();
                    SoundEffects.tocarSucesso();

                    registrarResultadoPartida(
                            dependenteId,
                            this.id,
                            infoJogos.getAcertos(),
                            infoJogos.getErros(),
                            timer.getTime()
                    );

                    int corTema = ContextCompat.getColor(JogoVogais.this, R.color.blue);
                    GameResultDialog.mostrarModalFeedback(this, true, corTema);
                }

            } else {
                SoundEffects.tocarErro();
                infoJogos.setErros(infoJogos.getErros() + 1);
            }
        });

        infoJogos.comecarJogo();

        SoundEffects.init(this);
        BackgroundMusics.start(this, R.raw.music4);
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
