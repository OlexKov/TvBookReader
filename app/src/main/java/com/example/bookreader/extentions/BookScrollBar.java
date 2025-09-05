package com.example.bookreader.extentions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.ProcessRuner;

import lombok.Getter;

public class BookScrollBar extends View {
    private Paint backgroundPaint;
    private Paint thumbPaint;
    private Paint textPaint;
    private int thumbHeight = 100;
    private final int minThumbHeight = 70;
    private final ProcessRuner runer = new ProcessRuner();
    private boolean isVisible = false;
    private final int FADEOUT_DELAY = 3000;
    private final int FADEIN_ANIMATION_TIME = 500;
    private final int FADEOUT_ANIMATION_TIME = 2000;
    private final int BAR_WIDTH_DP = 4;

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
        backgroundPaint.setColor(Color.argb(128, 211, 211, 211));
        thumbPaint = new Paint();
        thumbPaint.setColor(Color.DKGRAY);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#818181"));
        textPaint.setTextSize(50f);
        textPaint.setAntiAlias(true);
        setAlpha(0f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        int barLeft = width - AnimHelper.convertToPx(getContext(), BAR_WIDTH_DP);
        canvas.drawRect(barLeft, 0, width, height, backgroundPaint);
        float fraction = (max > 0) ? (progress / max) : 0f;
        int top = (int) ((height - thumbHeight) * fraction);
        canvas.drawRect(barLeft, top, width, top + thumbHeight, thumbPaint);

        String text = String.valueOf((int) progress);
        float textWidth = textPaint.measureText(text);
        float textX = barLeft - AnimHelper.convertToPx(getContext(),5) - textWidth;
        float textY = top + thumbHeight / 2f + Math.abs(textPaint.ascent() + textPaint.descent()) / 2f;

        canvas.drawText(text, textX, textY, textPaint);
    }

    public void setProgress(float progress) {
        if(progress != this.progress){
            this.progress = Math.max(0, Math.min(progress, max));
            invalidate();
            fadeIn();
            runer.runDelayed(FADEOUT_DELAY, this::fadeOut);
        }

    }

    public void setMax(float max){
        this.max = max;
        thumbHeight = (int) Math.max(minThumbHeight, getHeight() / (max + 1));
    }

    private void fadeIn() {
        if(!isVisible){
            isVisible = true;
            animate().alpha(1f).setDuration(FADEIN_ANIMATION_TIME).start();
        }
    }

    private void fadeOut() {
        if(isVisible){
            isVisible = false;
            animate().alpha(0f).setDuration(FADEOUT_ANIMATION_TIME).start();
        }
    }
}
