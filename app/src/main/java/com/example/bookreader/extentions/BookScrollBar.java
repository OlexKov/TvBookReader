package com.example.bookreader.extentions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import lombok.Getter;
import lombok.Setter;

public class BookScrollBar extends View {
    private Paint backgroundPaint;
    private Paint thumbPaint;
    private int thumbHeight = 100;
    private int minThumbHeight = 50;

    private float max = 100f; // максимальне значення прогресу

    @Getter
    private float progress = 0f; // поточне значення

    public BookScrollBar(Context context) {
        super(context);
        init();
    }

    public BookScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);

        thumbPaint = new Paint();
        thumbPaint.setColor(Color.DKGRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // фон
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // нормалізація прогресу до 0..1
        float fraction = (max > 0) ? (progress / max) : 0f;

        // позиція повзунка
        int top = (int) ((getHeight() - thumbHeight) * fraction);

        // повзунок
        canvas.drawRect(0, top, getWidth(), top + thumbHeight, thumbPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(progress, max));
        invalidate();
    }

    public void setMax(float max){
        this.max = max;
        thumbHeight = (int) Math.max(minThumbHeight, getHeight() / (max + 1));
    }
}
