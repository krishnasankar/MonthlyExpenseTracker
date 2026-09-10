package com.expensetracker.monthly.ui.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.expensetracker.monthly.data.model.MonthlySpendBarData;
import com.expensetracker.monthly.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiMonthBarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint benchmarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyStatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<MonthlySpendBarData> dataList = new ArrayList<>();
    private final List<RectF> barHitRects = new ArrayList<>();

    private String currencySymbol = "₹";
    private int selectedIndex = -1;
    private float animationProgress = 1.0f;
    private ValueAnimator animator;

    private int primaryColor;
    private int primaryContainerColor;
    private int surfaceVariantColor;
    private int onSurfaceColor;
    private int onSurfaceVariantColor;

    public interface OnMonthSelectedListener {
        void onMonthSelected(@Nullable MonthlySpendBarData data);
    }

    private OnMonthSelectedListener onMonthSelectedListener;

    public MultiMonthBarChartView(Context context) {
        super(context);
        init();
    }

    public MultiMonthBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiMonthBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        primaryColor = com.google.android.material.color.MaterialColors.getColor(
                this, androidx.appcompat.R.attr.colorPrimary, Color.parseColor("#1E88E5"));
        primaryContainerColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorSecondaryContainer, Color.parseColor("#BBDEFB"));
        surfaceVariantColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorSurfaceVariant, Color.parseColor("#E0E0E0"));
        onSurfaceColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurface, Color.parseColor("#212121"));
        onSurfaceVariantColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.parseColor("#757575"));

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(primaryContainerColor);

        activeBarPaint.setStyle(Paint.Style.FILL);
        activeBarPaint.setColor(primaryColor);

        selectedBarPaint.setStyle(Paint.Style.STROKE);
        selectedBarPaint.setStrokeWidth(dpToPx(2.5f));
        selectedBarPaint.setColor(onSurfaceColor);

        emptyBarPaint.setStyle(Paint.Style.FILL);
        emptyBarPaint.setColor(surfaceVariantColor);

        baselinePaint.setStyle(Paint.Style.STROKE);
        baselinePaint.setStrokeWidth(dpToPx(1f));
        baselinePaint.setColor(surfaceVariantColor);

        benchmarkPaint.setStyle(Paint.Style.STROKE);
        benchmarkPaint.setStrokeWidth(dpToPx(1.5f));
        benchmarkPaint.setColor(onSurfaceVariantColor);
        benchmarkPaint.setPathEffect(new DashPathEffect(new float[]{dpToPx(4), dpToPx(4)}, 0));

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(onSurfaceVariantColor);
        labelPaint.setTextSize(spToPx(11));

        activeLabelPaint.setTextAlign(Paint.Align.CENTER);
        activeLabelPaint.setColor(primaryColor);
        activeLabelPaint.setFakeBoldText(true);
        activeLabelPaint.setTextSize(spToPx(12));

        tooltipBgPaint.setStyle(Paint.Style.FILL);
        tooltipBgPaint.setColor(onSurfaceColor);

        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(spToPx(10.5f));

        tooltipAmountPaint.setTextAlign(Paint.Align.CENTER);
        tooltipAmountPaint.setColor(Color.WHITE);
        tooltipAmountPaint.setFakeBoldText(true);
        tooltipAmountPaint.setTextSize(spToPx(12));

        emptyStatePaint.setTextAlign(Paint.Align.CENTER);
        emptyStatePaint.setColor(onSurfaceVariantColor);
        emptyStatePaint.setTextSize(spToPx(13));
    }

    public void setOnMonthSelectedListener(OnMonthSelectedListener listener) {
        this.onMonthSelectedListener = listener;
    }

    public void setData(List<MonthlySpendBarData> items, String currencySymbol) {
        this.dataList.clear();
        this.barHitRects.clear();
        this.selectedIndex = -1;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "₹";

        if (items != null) {
            this.dataList.addAll(items);
        }

        startAnimation();
    }

    private void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(700);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = dpToPx(210);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height = desiredHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        }

        setMeasuredDimension(width > 0 ? width : dpToPx(300), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = dpToPx(16);
        float paddingRight = dpToPx(16);
        float paddingTop = dpToPx(42); // Space for tooltips and benchmark label
        float paddingBottom = dpToPx(32); // Space for month labels

        float plotWidth = width - paddingLeft - paddingRight;
        float plotHeight = height - paddingTop - paddingBottom;
        float baselineY = height - paddingBottom;

        if (dataList.isEmpty()) {
            canvas.drawText("No spending history available", width / 2f, height / 2f, emptyStatePaint);
            return;
        }

        int count = dataList.size();
        barHitRects.clear();

        // Calculate max amount and average
        double maxAmount = 0.0;
        double sum = 0.0;
        int activeMonths = 0;
        for (MonthlySpendBarData item : dataList) {
            if (item.getTotalSpend() > maxAmount) {
                maxAmount = item.getTotalSpend();
            }
            if (item.getTotalSpend() > 0) {
                sum += item.getTotalSpend();
                activeMonths++;
            }
        }

        double average = activeMonths > 0 ? (sum / activeMonths) : 0.0;
        double displayMax = Math.max(maxAmount * 1.18, 100.0);

        // Draw baseline
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, baselinePaint);

        // Draw average benchmark line if we have spending
        if (average > 0) {
            float benchmarkY = baselineY - (float) ((average / displayMax) * plotHeight * animationProgress);
            benchmarkY = Math.max(paddingTop + dpToPx(6), Math.min(baselineY - dpToPx(2), benchmarkY));
            canvas.drawLine(paddingLeft, benchmarkY, width - paddingRight, benchmarkY, benchmarkPaint);

            String avgText = "Avg " + CurrencyUtils.formatAmount(average, currencySymbol);
            benchmarkPaint.setTextSize(spToPx(10));
            canvas.drawText(avgText, width - paddingRight - dpToPx(4), benchmarkY - dpToPx(4), benchmarkPaint);
        }

        float slotWidth = plotWidth / count;
        float maxBarWidth = dpToPx(26);
        float barWidth = Math.min(slotWidth * 0.58f, maxBarWidth);
        float cornerRadius = dpToPx(6);

        // Draw bars and labels
        for (int i = 0; i < count; i++) {
            MonthlySpendBarData item = dataList.get(i);
            float slotCenterX = paddingLeft + (i + 0.5f) * slotWidth;
            float left = slotCenterX - barWidth / 2f;
            float right = slotCenterX + barWidth / 2f;

            float barH = (float) ((item.getTotalSpend() / displayMax) * plotHeight * animationProgress);
            if (item.getTotalSpend() > 0 && barH < dpToPx(4)) {
                barH = dpToPx(4); // Minimum visible bar
            }

            float top = baselineY - barH;

            // Hit rect for touch detection (full vertical slot)
            RectF hitRect = new RectF(left - dpToPx(6), paddingTop, right + dpToPx(6), baselineY + dpToPx(16));
            barHitRects.add(hitRect);

            if (item.getTotalSpend() > 0) {
                Paint currentPaint = item.isCurrentSelection() ? activeBarPaint : barPaint;
                RectF barRect = new RectF(left, top, right, baselineY);
                drawRoundedTopBar(canvas, barRect, cornerRadius, currentPaint);

                if (i == selectedIndex) {
                    // Highlight outline
                    drawRoundedTopBarOutline(canvas, barRect, cornerRadius, selectedBarPaint);
                }
            } else {
                // Empty hairline indicator
                RectF emptyRect = new RectF(left, baselineY - dpToPx(3), right, baselineY);
                canvas.drawRoundRect(emptyRect, dpToPx(2), dpToPx(2), emptyBarPaint);
            }

            // Month Label below baseline
            Paint txtPaint = item.isCurrentSelection() ? activeLabelPaint : labelPaint;
            canvas.drawText(item.getShortMonthLabel(), slotCenterX, baselineY + dpToPx(18), txtPaint);
        }

        // Draw Tooltip for selected bar if any
        if (selectedIndex >= 0 && selectedIndex < count) {
            MonthlySpendBarData sel = dataList.get(selectedIndex);
            float slotCenterX = paddingLeft + (selectedIndex + 0.5f) * slotWidth;
            float barH = (float) ((sel.getTotalSpend() / displayMax) * plotHeight * animationProgress);
            float barTop = baselineY - barH;

            drawTooltip(canvas, slotCenterX, barTop, sel.getFullMonthLabel(),
                    CurrencyUtils.formatAmount(sel.getTotalSpend(), currencySymbol), width);
        }
    }

    private void drawRoundedTopBar(Canvas canvas, RectF rect, float radius, Paint paint) {
        Path path = new Path();
        path.moveTo(rect.left, rect.bottom);
        path.lineTo(rect.left, rect.top + radius);
        path.quadTo(rect.left, rect.top, rect.left + radius, rect.top);
        path.lineTo(rect.right - radius, rect.top);
        path.quadTo(rect.right, rect.top, rect.right, rect.top + radius);
        path.lineTo(rect.right, rect.bottom);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRoundedTopBarOutline(Canvas canvas, RectF rect, float radius, Paint paint) {
        Path path = new Path();
        path.moveTo(rect.left, rect.bottom);
        path.lineTo(rect.left, rect.top + radius);
        path.quadTo(rect.left, rect.top, rect.left + radius, rect.top);
        path.lineTo(rect.right - radius, rect.top);
        path.quadTo(rect.right, rect.top, rect.right, rect.top + radius);
        path.lineTo(rect.right, rect.bottom);
        canvas.drawPath(path, paint);
    }

    private void drawTooltip(Canvas canvas, float anchorX, float anchorY, String title, String amount, int screenWidth) {
        float tooltipWidth = dpToPx(96);
        float tooltipHeight = dpToPx(36);
        float margin = dpToPx(8);

        float left = anchorX - tooltipWidth / 2f;
        // Clamp to screen bounds
        if (left < margin) {
            left = margin;
        } else if (left + tooltipWidth > screenWidth - margin) {
            left = screenWidth - margin - tooltipWidth;
        }

        float top = anchorY - tooltipHeight - dpToPx(8);
        if (top < dpToPx(4)) {
            top = anchorY + dpToPx(10); // flip below if not enough room above
        }

        RectF tooltipRect = new RectF(left, top, left + tooltipWidth, top + tooltipHeight);
        canvas.drawRoundRect(tooltipRect, dpToPx(8), dpToPx(8), tooltipBgPaint);

        float centerX = tooltipRect.centerX();
        canvas.drawText(title, centerX, top + dpToPx(13), tooltipTextPaint);
        canvas.drawText(amount, centerX, top + dpToPx(28), tooltipAmountPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            int clicked = -1;
            for (int i = 0; i < barHitRects.size(); i++) {
                if (barHitRects.get(i).contains(x, y)) {
                    clicked = i;
                    break;
                }
            }

            if (clicked != -1) {
                selectedIndex = (selectedIndex == clicked) ? -1 : clicked;
                invalidate();
                if (onMonthSelectedListener != null) {
                    onMonthSelectedListener.onMonthSelected(selectedIndex >= 0 ? dataList.get(selectedIndex) : null);
                }
                return true;
            } else if (selectedIndex != -1) {
                selectedIndex = -1;
                invalidate();
                if (onMonthSelectedListener != null) {
                    onMonthSelectedListener.onMonthSelected(null);
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }
}
