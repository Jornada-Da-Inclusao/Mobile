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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.games.components.KeyView;
import com.example.integra_kids_mobile.games.components.KeyViewStateEnum;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.games.components.jogo_numeros.NumeroView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jogo_numeros);


        Timer timer = new Timer(this);
        FrameLayout rootLayout = findViewById(R.id.timer);
        rootLayout.addView(timer);
        timer.startTimer();

        final GridLayout gridLayout = findViewById(R.id.numeros_grid);

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

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, 200);
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
            numberView.setOnClickListener(v -> {
                final TextView kv = (TextView) v;
                if (! kv.isFocusable()) {
                    return;
                }

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
}
