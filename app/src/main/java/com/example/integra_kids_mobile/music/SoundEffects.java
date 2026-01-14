package com.example.integra_kids_mobile.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.integra_kids_mobile.R;

public class SoundEffects {
    private static SoundPool soundPool;
    private static int somClique, somAcerto, somErro, somSucesso, somFracasso;

    public static void init(Context context) {
        // Configuração moderna do SoundPool
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5) // Toca até 5 sons ao mesmo tempo
                .setAudioAttributes(attrs)
                .build();

        // Carrega o som na memória (substitua pelo seu arquivo)
        somClique = soundPool.load(context, R.raw.button_click, 1);
        somAcerto = soundPool.load(context, R.raw.game_correct_answer, 1);
        somErro = soundPool.load(context, R.raw.game_wrong_answer, 1);
        somSucesso = soundPool.load(context, R.raw.game_complete, 1);
        somFracasso = soundPool.load(context, R.raw.game_not_complete, 1);
    }

    public static void tocarClique() {
        if (soundPool != null) {
            soundPool.play(somClique, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    public static void tocarAcerto() {
        if (soundPool != null) {
            soundPool.play(somAcerto, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    public static void tocarErro() {
        if (soundPool != null) {
            soundPool.play(somErro, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    public static void tocarSucesso() {
        if (soundPool != null) {
            soundPool.play(somSucesso, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    public static void tocarFalha() {
        if (soundPool != null) {
            soundPool.play(somFracasso, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}