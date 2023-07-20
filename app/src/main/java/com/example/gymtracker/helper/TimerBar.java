package com.example.gymtracker.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.gymtracker.R;

public class TimerBar extends View {
    private Paint backgroundPaint;
    private Paint red;
    private Paint progressPaint;
    private float progress;

    public TimerBar(Context context) {
        super(context);
        init();
    }

    public TimerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.background));

        red = new Paint();
        red.setColor(getResources().getColor(R.color.red));

        progressPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Berechne den aktuellen Fortschritt in Pixeln
        float progressWidth = width * progress;

        // Zeichne den Hintergrund
        if (this.progress > 0)
            canvas.drawRect(0, 0, width, height, backgroundPaint);
        else
            canvas.drawRect(0, 0, width, height, red);


        // Aktualisiere den Farbverlauf des Fortschrittsbalkens
        int[] colors = {Color.RED, Color.GREEN};
        float[] positions = {0, 1 - progress};
        Shader shader = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
        progressPaint.setShader(shader);

        // Zeichne den Fortschrittsbalken
        canvas.drawRect(0, 0, progressWidth, height, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
        invalidate(); // Aktualisiere die View
    }

    public float getProgress() {
        return this.progress;
    }
}