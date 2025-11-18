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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.InfoJogos;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jogo_vogais);

        Timer timer = new Timer(this);
        FrameLayout rootLayout = findViewById(R.id.timer);
        rootLayout.addView(timer);
        timer.startTimer();

        final GridLayout gridLayout = findViewById(R.id.vogais_grid);

        for (int i = 'a'; i <= 'z'; i++) {
            // Crie o numero
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

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(140, 140);
            params.gravity = Gravity.TOP | Gravity.END;
            params.setMargins(0, 40, 40, 0);
            letterView.setLayoutParams(params);

            // Coloque o numero no grid
            final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            gridLayout.addView(letterView);
            letterBoxes[size++] = letterView;

            GridLayout placedNumbersContainer = findViewById(R.id.placed_vogais);

            // Coloque a vogal no seu respectivo container
            placedNumbersContainer.setOnClickListener(v -> {
                TextView n = letterBoxes[vogais[letterToBePlaced] - 'a'];
                infoJogos.setTentativas(infoJogos.getTentativas() + 1);

                if (this.selectedLetterBoxIdx == vogais[letterToBePlaced] - 'a') {
                    infoJogos.setAcertos(infoJogos.getAcertos() + 1);
                    placedKeyViews += 1;

                    final ViewGroup oldParent = (ViewGroup) n.getParent();
                    final GridLayout newParent = (GridLayout) v;

                    // Animação entre ViewGroups. Importante ser uma Transition e não qualquer
                    // animação normal (seja com ValueAnimator ou animação de View), caso contrário
                    // o objeto é cortado se sair do parent
                    Transition move = new ChangeTransform().addTarget(n).setDuration(300);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.vogais_root), move);
                    oldParent.removeView(n);
                    newParent.addView(n);
                    letterToBePlaced++;

                    n.setFocusable(false);

                    if (placedKeyViews == vogais.length) {
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
                } else {
                    infoJogos.setErros(infoJogos.getErros() + 1);
                }
            });

            // Faça highlight no círculo caso selecionado
            letterView.setOnClickListener(v -> {
                final TextView kv = (TextView) v;
                if (! kv.isFocusable()) {
                    return;
                }

                GradientDrawable _drawable;

                // Restaure a cor do colorbox se já tocado
                if (this.selectedLetterBoxIdx == kv.getId()) {
                    _drawable = (GradientDrawable) kv.getBackground();
                    _drawable.setColor(Color.parseColor(colors[currentColor++ % 2]));
                    return;
                }

                // Restaure a cor do colorbox anterior
                if (this.selectedLetterBoxIdx != -1) {
                    _drawable = (GradientDrawable) letterBoxes[this.selectedLetterBoxIdx].getBackground();
                    _drawable.setColor(Color.parseColor(colors[0]));
                }

                this.selectedLetterBoxIdx = kv.getId();
                _drawable = (GradientDrawable) letterBoxes[this.selectedLetterBoxIdx].getBackground();
                _drawable.setColor(Color.parseColor(colors[1]));
            });
        }

        infoJogos.comecarJogo();
    }
}
