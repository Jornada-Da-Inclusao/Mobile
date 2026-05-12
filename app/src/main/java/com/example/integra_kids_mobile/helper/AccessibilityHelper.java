package com.example.integra_kids_mobile.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AccessibilityHelper {

    private static final String PREFS = "AppPrefs";

    // ─────────────────────────────────────────
    // 🎨 FILTRO DE DALTONISMO
    // Chame depois do setContentView() em cada Activity
    // ─────────────────────────────────────────
    public static void applyColorblindFilter(Object target) {
        View root;
        Context context;

        if (target instanceof Activity) {
            root = ((Activity) target).findViewById(android.R.id.content);
            context = (Activity) target;
        } else if (target instanceof View) {
            root = (View) target;
            context = root.getContext();
        } else {
            return; // tipo não suportado, ignora
        }

        String mode = prefs(context).getString("colorblindMode", "NONE");

        float[] matrix;
        switch (mode) {
            case "PROTANOMALIA":
                matrix = new float[]{
                        0.567f, 0.433f, 0,     0, 0,
                        0.558f, 0.442f, 0,     0, 0,
                        0,      0.242f, 0.758f,0, 0,
                        0,      0,      0,     1, 0
                };
                break;
            case "DEUTERANOMALIA":
                matrix = new float[]{
                        0.625f, 0.375f, 0,     0, 0,
                        0.700f, 0.300f, 0,     0, 0,
                        0,      0.300f, 0.700f,0, 0,
                        0,      0,      0,     1, 0
                };
                break;
            case "TRITANOMALIA":
                // Dificuldade com azul/amarelo
                matrix = new float[]{
                        0.950f, 0.050f, 0,     0, 0,
                        0,      0.433f, 0.567f,0, 0,
                        0,      0.475f, 0.525f,0, 0,
                        0,      0,      0,     1, 0
                };
                break;
            default:
                root.setLayerType(View.LAYER_TYPE_NONE, null);
                return;
        }

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(matrix)));
        root.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    // ─────────────────────────────────────────
    // 🔤 TAMANHO DO TEXTO
    // Chame no attachBaseContext() de cada Activity
    // ─────────────────────────────────────────
    // Para Activity — no attachBaseContext()
    public static Context applyFontScale(Context context) {
        int progress = prefs(context).getInt("textSizeProgress", 50);
        float fontScale = 0.85f + (progress / 100f) * 0.55f;

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.fontScale = fontScale;
        return context.createConfigurationContext(config);
    }

    // Para Fragment e Dialog — percorre todos os TextViews e redimensiona
    public static void applyFontScale(View root) {
        Context context = root.getContext();
        int progress = prefs(context).getInt("textSizeProgress", 50);

        // Escala relativa: 50 = sem mudança (1.0), 0 = menor, 100 = maior
        float scale = 0.85f + (progress / 100f) * 0.55f;

        applyFontScaleToViews(root, scale);
    }

    private static void applyFontScaleToViews(View view, float scale) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            // Lê o tamanho original em SP e aplica a escala
            float originalSp = tv.getTextSize() / tv.getContext().getResources()
                    .getDisplayMetrics().scaledDensity;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, originalSp * scale);

        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyFontScaleToViews(group.getChildAt(i), scale);
            }
        }
    }

    // ─────────────────────────────────────────
    // 📳 VIBRAÇÃO
    // Chame em qualquer evento que mereça feedback
    // ─────────────────────────────────────────
    public static void vibrate(Context context) {
        if (!prefs(context).getBoolean("haptic", false)) return;

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(48, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(48);
        }
    }

    // ─────────────────────────────────────────
    // Utilitário interno
    // ─────────────────────────────────────────
    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}