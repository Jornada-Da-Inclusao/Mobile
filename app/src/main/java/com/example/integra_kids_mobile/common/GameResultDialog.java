package com.example.integra_kids_mobile.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.helper.AccessibilityHelper;
import com.example.integra_kids_mobile.music.BackgroundMusics;
import com.example.integra_kids_mobile.music.SoundEffects;
import com.example.integra_kids_mobile.profile.PerfilResultados;

public class GameResultDialog {
        public static void mostrarModalFeedback(Activity activity, boolean sucesso, int corTema) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_resultado_jogo, null);
            AccessibilityHelper.applyColorblindFilter(dialogView);

            AlertDialog dialog = new AlertDialog.Builder(activity).create();
            dialog.setView(dialogView);

            // Impede fechar ao clicar fora do modal
            dialog.setCanceledOnTouchOutside(false);
            // Impede fechar ao usar o botão "voltar" do celular
            dialog.setCancelable(false);

            ImageView imgStatus = dialogView.findViewById(R.id.imgStatus);
            TextView tvMensagem = dialogView.findViewById(R.id.tvMensagem);
            Button btnRepetir = dialogView.findViewById(R.id.btnRepetir);
            Button btnResultados = dialogView.findViewById(R.id.btnResultados);
            Button btnMenu = dialogView.findViewById(R.id.btnMenu);

            btnRepetir.setBackgroundTintList(android.content.res.ColorStateList.valueOf(corTema));

            if (sucesso) {
                BackgroundMusics.stop();
                SoundEffects.tocarSucesso();
                tvMensagem.setText("Incrível! Você conseguiu!");
                imgStatus.setImageResource(R.drawable.icon_sucess);
            } else {
                BackgroundMusics.stop();
                SoundEffects.tocarFalha();
                tvMensagem.setText("Quase lá! Tente de novo!");
                imgStatus.setImageResource(R.drawable.icon_failed);
            }

            btnRepetir.setOnClickListener(v -> {
                dialog.dismiss();
                activity.recreate(); // Reinicia a fase atual
            });

            btnResultados.setOnClickListener(v -> {
                dialog.dismiss();
                activity.startActivity(new Intent(activity, PerfilResultados.class));
                activity.finish();
            });

            btnMenu.setOnClickListener(v -> {
                dialog.dismiss();
                activity.finish(); // Fecha o jogo e volta para o menu de seleção
            });

            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
}
