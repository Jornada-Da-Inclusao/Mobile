package com.example.integra_kids_mobile.games.views.jogo_cores;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.games.components.KeyView;
import com.example.integra_kids_mobile.games.components.KeyViewStateEnum;
import com.example.integra_kids_mobile.games.components.Timer;
import com.example.integra_kids_mobile.games.components.jogo_cores.ColorView;
import com.example.integra_kids_mobile.games.InfoJogos;
import com.example.integra_kids_mobile.API.GameService;
import com.example.integra_kids_mobile.music.BackgroundMusics;
import com.example.integra_kids_mobile.music.SoundEffects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JogoCores extends AppCompatActivity {
    private final long id = 4;
    private final InfoJogos infoJogos = new InfoJogos(this.id, 0); // hardcoded
    private final List<ColorViewState> colorViewState = new ArrayList<>();
    private int placedColorBoxes = 0;
    private int selectedColorBoxIdx = -1;
    private int dependenteId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jogo_cores);
        dependenteId = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
            .getInt("selected_player_id", -1);

        ReturnButton.configurar(this);

        String[][] colors = {
                { "#E9E9E9", "#B8B6B6" }, // branco
                { "#FA70E4", "#CC5CBA" }, // rosa
                { "#8FF43F", "#78BD42" }, // verde
                { "#FAA94B", "#BF8A4D" }, // laranja
                { "#FFE96A", "#BFB15C" }, // amarelo
                { "#EB4A4A", "#B84444" }  // vermelho
        };
        FrameLayout[] containers = {
                findViewById(R.id.coelho_slot),
                findViewById(R.id.borboleta_slot),
                findViewById(R.id.sapo_slot),
                findViewById(R.id.leao_slot),
                findViewById(R.id.peixe_slot),
                findViewById(R.id.joaninha_slot),
        };

        Timer timer = new Timer(this);
        FrameLayout rootLayout = findViewById(R.id.timer);
        rootLayout.addView(timer);
        timer.startTimer();

        final GridLayout gridLayout = findViewById(R.id.cores_grid);

        for (int i = 0; i < colors.length; i++) {
            // Crie a cor
            final ColorViewState currentData = new ColorViewState.Builder()
                    .withId(i)
                    .withContainer(containers[i])
                    .withColorView(new ColorView(this))
                    .build();

            colorViewState.add(currentData);

            final ColorView colorView = currentData.getColorView();
            colorView.setId(i);
            colorView.setWidth(75);
            colorView.setHeight(75);
            colorView.setBorderSize(4);
            colorView.setColors(colors[i]);
            colorView.setBackgroundColor(Color.parseColor(colorView.getColors()[KeyViewStateEnum.NORMAL]));
            colorView.setStateListAnimator(null);

            // Coloque a cor no grid
            final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            gridParams.setGravity(Gravity.BOTTOM);

            final int margin = (int) (colorView.getKeyWidth() * 0.20);
            gridParams.setMargins(margin, margin, margin, margin);

            colorView.setLayoutParams(gridParams);
            gridLayout.addView(colorView);

            final View container = currentData.getContainer();
            // Coloque a cor no seu respectivo container
            container.setOnClickListener(v -> {
                SoundEffects.tocarClique();
                final ColorViewState cvs = colorViewState.get(this.selectedColorBoxIdx);
                final ColorView cv = cvs.getColorView();

                infoJogos.setTentativas(infoJogos.getTentativas() + 1);

                if (this.selectedColorBoxIdx == currentData.getId() && !cv.isPlaced()) {

                    SoundEffects.tocarAcerto();
                    infoJogos.setAcertos(infoJogos.getAcertos() + 1);
                    placedColorBoxes += 1;

                    final ViewGroup oldParent = (ViewGroup) cv.getParent();
                    final FrameLayout newParent = (FrameLayout) v;

                    // Animação entre ViewGroups. Importante ser uma Transition e não qualquer
                    // animação normal (seja com ValueAnimator ou animação de View), caso contrário
                    // o objeto é cortado se sair do parent
                    Transition move = new ChangeTransform().addTarget(cv).setDuration(300);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.cores_root), move);
                    oldParent.removeView(cv);
                    newParent.addView(cv);

                    cv.setFocusable(false);
                    cv.setPlaced(true);

                    String color = cv.getColors()[KeyViewStateEnum.NORMAL];
                    cv.setBackgroundColor(Color.parseColor(color));

                    if (placedColorBoxes == colorViewState.toArray().length) {

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
                    SoundEffects.tocarErro();
                    infoJogos.setErros(infoJogos.getErros() + 1);
                }
            });

            // Faça highlight no círculo caso selecionado
            colorView.setOnClickListener(v -> {
                final KeyView kv = (KeyView) v;
                if (kv.isPlaced()) {
                    return;
                }
                SoundEffects.tocarClique();

                // Restaure a cor do colorbox se já tocado
                if (this.selectedColorBoxIdx == kv.getId()) {
                    colorViewState.get(this.selectedColorBoxIdx).getColorView().toggleColor();
                    return;
                }

                // Restaure a cor do colorbox anterior
                if (this.selectedColorBoxIdx != -1) {
                    colorViewState.get(this.selectedColorBoxIdx).getColorView().setNormalColor();
                }

                this.selectedColorBoxIdx = kv.getId();
                colorViewState.get(this.selectedColorBoxIdx).getColorView().setFocusedColor();
            });
        }

        infoJogos.comecarJogo();

        SoundEffects.init(this);
        BackgroundMusics.start(this, R.raw.music3);
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
