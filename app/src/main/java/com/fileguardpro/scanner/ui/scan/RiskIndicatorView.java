package com.fileguardpro.scanner.ui.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RiskIndicatorView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF arcRect;
    private int riskScore = 0;
    private int riskColor = 0xFF4CAF50;
    private float strokeWidth = 20f;

    public RiskIndicatorView(Context context) {
        super(context);
        init();
    }

    public RiskIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RiskIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(0x30000000);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(riskColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFF212121);
        textPaint.setTextSize(48f);

        arcRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = strokeWidth / 2 + 10;
        arcRect.set(padding, padding, getWidth() - padding, getHeight() - padding);

        // Draw background arc
        canvas.drawArc(arcRect, 135, 270, false, backgroundPaint);

        // Draw progress arc
        float sweepAngle = (riskScore / 100f) * 270f;
        progressPaint.setColor(riskColor);
        canvas.drawArc(arcRect, 135, sweepAngle, false, progressPaint);

        // Draw score text
        float textX = getWidth() / 2f;
        float textY = getHeight() / 2f + textPaint.getTextSize() / 3;
        canvas.drawText(String.valueOf(riskScore), textX, textY, textPaint);
    }

    public void setRiskScore(int score) {
        this.riskScore = Math.max(0, Math.min(100, score));
        invalidate();
    }

    public void setRiskColor(int color) {
        this.riskColor = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        if (size == 0) size = 200;
        setMeasuredDimension(size, size);
    }
}
