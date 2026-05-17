package com.example.integra_kids_mobile.ui.chatbot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AvatarView extends View {

    private static final int COLS = 5;
    private static final int ROWS = 3;

    public enum Expression {
        HAPPY       (0, 0),
        NEUTRAL     (1, 0),
        THINKING    (2, 0),
        EXCITED     (3, 0),
        SAD         (4, 0),
        LISTENING   (0, 1),
        LAUGHING    (1, 1),
        SEARCHING   (2, 1),
        CONFUSED    (3, 1),
        SATISFIED   (4, 1),
        INDIFFERENT (0, 2),
        EMBARRASSED (1, 2),
        SLEEPING    (2, 2),
        SURPRISED   (3, 2);

        final int col, row;
        Expression(int col, int row) { this.col = col; this.row = row; }
    }

    private Bitmap spritesheet;
    private Expression currentExpression = Expression.HAPPY;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    // Calculados a partir do bitmap real, não fixos
    private int cellW = 0;
    private int cellH = 0;

    public AvatarView(Context context) {
        super(context); init(context);
    }
    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs); init(context);
    }
    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init(context);
    }

    private void init(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; // impede o Android de redimensionar pelo dpi

        int resId = context.getResources().getIdentifier(
                "avatar_spritesheet", "drawable", context.getPackageName());
        if (resId != 0) {
            spritesheet = BitmapFactory.decodeResource(context.getResources(), resId, opts);
            cellW = spritesheet.getWidth()  / COLS;
            cellH = spritesheet.getHeight() / ROWS;
        }
    }

    public void setExpression(String expression) {
        currentExpression = Expression.valueOf(expression.toUpperCase());
        invalidate();
    }

    public void setExpression(Expression expression) {
        currentExpression = expression;
        invalidate();
    }

    public Expression getExpression() {
        return currentExpression;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (spritesheet == null || cellW == 0) return;

        int srcX = currentExpression.col * cellW;
        int srcY = currentExpression.row * cellH;
        srcRect.set(srcX, srcY, srcX + cellW, srcY + cellH);
        dstRect.set(0, 0, getWidth(), getHeight());

        canvas.drawBitmap(spritesheet, srcRect, dstRect, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }
}