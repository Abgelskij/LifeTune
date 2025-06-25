package com.example.lifetune;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class SleepTimelineView extends View {
    private Paint circlePaint;
    private Paint sleepArcPaint;
    private Paint timeIndicatorPaint;
    private Paint textPaint;

    private float bedtimeHour = 22.75f; // 22:45
    private float wakeupHour = 7.25f;   // 07:15
    private float currentHour = 0f;

    public SleepTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#2A2A2A")); // чуть светлее фона
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(8f); // тоньше обводка
        circlePaint.setAntiAlias(true);

        sleepArcPaint = new Paint();
        sleepArcPaint.setColor(Color.parseColor("#663A80F9")); // тёмно-синий, 40% прозрачности
        sleepArcPaint.setStyle(Paint.Style.STROKE);
        sleepArcPaint.setStrokeWidth(20f); // тоньше
        sleepArcPaint.setAntiAlias(true);
        sleepArcPaint.setStrokeCap(Paint.Cap.ROUND);

        timeIndicatorPaint = new Paint();
        timeIndicatorPaint.setColor(Color.parseColor("#DDDDDD")); // светло-серый вместо ярко-белого
        timeIndicatorPaint.setStyle(Paint.Style.FILL);
        timeIndicatorPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#CCCCCC")); // мягкий белый
        textPaint.setTextSize(32f); // чуть меньше
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 150; // раньше было -60

        int centerX = width / 2;
        int centerY = height / 2;

        RectF oval = new RectF(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // Углы дуги сна
        float startAngle = hourToAngle(bedtimeHour);
        float sweepAngle = calculateSweep(bedtimeHour, wakeupHour);

        canvas.drawArc(oval, startAngle, sweepAngle, false, sleepArcPaint);

        // Индикатор текущего времени
        float currentAngle = hourToAngle(currentHour);
        float indicatorRadius = radius + 20;
        float indicatorX = (float) (centerX + indicatorRadius * Math.cos(Math.toRadians(currentAngle)));
        float indicatorY = (float) (centerY + indicatorRadius * Math.sin(Math.toRadians(currentAngle)));

        canvas.drawCircle(indicatorX, indicatorY, 12, timeIndicatorPaint);

        // Подписи под кругом
        canvas.drawText("Сон: " + formatTime(bedtimeHour) + " — " + formatTime(wakeupHour), centerX, centerY + radius + 80, textPaint);
        canvas.drawText("Текущее время: " + formatTime(currentHour), centerX, centerY + radius + 130, textPaint);
    }

    private float hourToAngle(float hour) {
        return (hour / 24f) * 360f - 90f;
    }

    private float calculateSweep(float start, float end) {
        float duration = end > start ? end - start : 24 + end - start;
        return (duration / 24f) * 360f;
    }

    private String formatTime(float hour) {
        int hours = (int) hour;
        int minutes = (int) ((hour - hours) * 60);
        return String.format("%02d:%02d", hours, minutes);
    }

    public void setTimes(float bedtimeHour, float wakeupHour) {
        this.bedtimeHour = bedtimeHour;
        this.wakeupHour = wakeupHour;
        invalidate();
    }

    public void setCurrentTime(float currentHour) {
        this.currentHour = currentHour;
        invalidate();
    }
}