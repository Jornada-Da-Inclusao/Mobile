package com.example.integra_kids_mobile.games.components.jogo_da_memoria;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.GridLayout;

import androidx.appcompat.widget.AppCompatButton;

public class Card extends AppCompatButton {

    private int value;
    private int id;
    private boolean isFaceUp;
    private boolean isMatched;

    private String[] emojis = {"🌟", "🎈", "🎨", "🎵"};

    public Card(Context context, int value, int id) {
        super(context);
        this.value = value;
        this.id = id;
        this.isFaceUp = false;
        this.isMatched = false;

        setupCard();
    }

    private void setupCard() {
        // Set card size
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80,
                getResources().getDisplayMetrics()
        );

        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 120,
                getResources().getDisplayMetrics()
        );

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = height;
        params.setMargins(8, 8, 8, 8);
        setLayoutParams(params);

        // Set card appearance
        setTextSize(32);
        setBackgroundColor(Color.parseColor("#2196F3"));
        setText("?");
        setTextColor(Color.WHITE);
        setAllCaps(false);
    }

    public void flip() {
        isFaceUp = !isFaceUp;

        if (isFaceUp) {
            setText(emojis[value - 1]);
            setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            setText("?");
            setBackgroundColor(Color.parseColor("#2196F3"));
        }
    }

    public int getValue() {
        return value;
    }

    public boolean isFaceUp() {
        return isFaceUp;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
        if (matched) {
            setBackgroundColor(Color.parseColor("#FF9800"));
            setEnabled(false);
        }
    }
}