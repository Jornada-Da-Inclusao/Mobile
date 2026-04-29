package com.example.integra_kids_mobile.games.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;

import android.os.Handler;
import android.widget.FrameLayout;

public class Timer extends androidx.appcompat.widget.AppCompatTextView {

    private final Handler handler = new Handler();
    private int elapsed = 0; // começa do zero e cresce

    private long startedAt = 0;

    public Timer(Context context) {
        super(context);
        init();
    }

    public interface TimerListener {
        void onTimeFinished();
    }

    private TimerListener listener;

    public void setTimerListener(TimerListener listener) {
        this.listener = listener;
    }

    public void startTimer() {
        startedAt = System.currentTimeMillis();
        handler.post(updateTimer);
    }

    public void stopTimer() {
        handler.removeCallbacks(updateTimer);
    }

    // Retorna o tempo decorrido em segundos
    public int getTime() {
        long now = System.currentTimeMillis();
        return (int) ((now - startedAt) / 1000);
    }

    private void init() {
        this.setTextColor(Color.WHITE);
        this.setTextSize(18);
        this.setGravity(Gravity.CENTER);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor("#EB4A4A"));
        this.setBackground(drawable);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.END;
        params.setMargins(0, 50, 10, 0);
        this.setLayoutParams(params);

        setTimerText(formatTimerString());
    }

    private String formatTimerString() {
        int minutes = (elapsed / 1000) / 60;
        int seconds = (elapsed / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setTimerText(String s) {
        this.setText(s);
    }

    private final Runnable updateTimer = new Runnable() {
        public void run() {
            elapsed += 1000; // cresce 1 segundo
            setTimerText(formatTimerString());
            handler.postDelayed(this, 1000);
        }
    };
}