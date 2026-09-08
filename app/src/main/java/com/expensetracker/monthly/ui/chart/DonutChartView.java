package com.expensetracker.monthly.ui.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerSubtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF chartBounds = new RectF();
    private final List<CategorySpendSummary> dataList = new ArrayList<>();
    private final List<Float> sweepAngles = new ArrayList<>();
    private final List<Float> startAngles = new ArrayList<>();

    private double totalAmount = 0.0;
    private String currencySymbol = "₹";
    private int selectedIndex = -1;
    private float animationProgress = 1.0f;
    private ValueAnimator animator;

    public interface OnCategorySelectedListener {
        void onCategorySelected(@Nullable CategorySpendSummary summary);
    }

    private OnCategorySelectedListener onCategorySelectedListener;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int surfaceColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorSurface, Color.WHITE);
        int onSurfaceColor = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurface, Color.parseColor("#212121"));
        int onSurfaceVariant = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.parseColor("#757575"));
        int primaryColor = com.google.android.material.color.MaterialColors.getColor(
                this, androidx.appcompat.R.attr.colorPrimary, Color.parseColor("#1E88E5"));
        int surfaceVariant = com.google.android.material.color.MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorSurfaceVariant, Color.parseColor("#E0E0E0"));

        arcPaint.setStyle(Paint.Style.FILL);

        centerHolePaint.setStyle(Paint.Style.FILL);
        centerHolePaint.setColor(surfaceColor);

        centerTitlePaint.setTextAlign(Paint.Align.CENTER);
        centerTitlePaint.setColor(onSurfaceVariant);
        centerTitlePaint.setTextSize(spToPx(13));

        centerAmountPaint.setTextAlign(Paint.Align.CENTER);
        centerAmountPaint.setColor(onSurfaceColor);
        centerAmountPaint.setFakeBoldText(true);
        centerAmountPaint.setTextSize(spToPx(20));

        centerSubtitlePaint.setTextAlign(Paint.Align.CENTER);
        centerSubtitlePaint.setColor(primaryColor);
        centerSubtitlePaint.setTextSize(spToPx(12));

        emptyRingPaint.setStyle(Paint.Style.STROKE);
        emptyRingPaint.setStrokeWidth(dpToPx(28));
        emptyRingPaint.setColor(surfaceVariant);
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.onCategorySelectedListener = listener;
    }

    public void setData(List<CategorySpendSummary> items, String currencySymbol) {
        this.dataList.clear();
        this.sweepAngles.clear();
        this.startAngles.clear();
        this.selectedIndex = -1;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "$";

        totalAmount = 0.0;
        if (items != null && !items.isEmpty()) {
            this.dataList.addAll(items);
            for (CategorySpendSummary item : items) {
                totalAmount += item.totalAmount;
            }

            float currentAngle = -90f; // Start at 12 o'clock
            for (CategorySpendSummary item : items) {
                float sweep = totalAmount > 0 ? (float) ((item.totalAmount / totalAmount) * 360f) : 0f;
                startAngles.add(currentAngle);
                sweepAngles.add(sweep);
                currentAngle += sweep;
            }
        }

        startAnimation();
    }

    private void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(750);
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
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width > 0 ? width : dpToPx(260), height > 0 ? height : dpToPx(260));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = dpToPx(16);
        chartBounds.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - dpToPx(16);
        float innerRadius = radius * 0.62f;

        if (dataList.isEmpty() || totalAmount <= 0) {
            // Draw empty ring
            canvas.drawCircle(cx, cy, (radius + innerRadius) / 2f, emptyRingPaint);

            canvas.drawText("No Expenses", cx, cy - dpToPx(6), centerTitlePaint);
            canvas.drawText(CurrencyUtils.formatAmount(0.0, currencySymbol), cx, cy + dpToPx(18), centerAmountPaint);
            return;
        }

        // Draw Slices
        for (int i = 0; i < dataList.size(); i++) {
            CategorySpendSummary item = dataList.get(i);
            float start = startAngles.get(i);
            float sweep = sweepAngles.get(i) * animationProgress;

            try {
                arcPaint.setColor(Color.parseColor(item.colorHex));
            } catch (Exception e) {
                arcPaint.setColor(Color.parseColor("#1E88E5"));
            }

            if (i == selectedIndex) {
                // Highlight expanded slice
                RectF expandedBounds = new RectF(chartBounds.left - dpToPx(6), chartBounds.top - dpToPx(6),
                        chartBounds.right + dpToPx(6), chartBounds.bottom + dpToPx(6));
                canvas.drawArc(expandedBounds, start, sweep, true, arcPaint);
            } else {
                canvas.drawArc(chartBounds, start, sweep, true, arcPaint);
            }
        }

        // Cutout hole for Donut effect
        canvas.drawCircle(cx, cy, innerRadius, centerHolePaint);

        // Center Content Text
        if (selectedIndex >= 0 && selectedIndex < dataList.size()) {
            CategorySpendSummary sel = dataList.get(selectedIndex);
            canvas.drawText(sel.categoryName, cx, cy - dpToPx(16), centerTitlePaint);
            canvas.drawText(CurrencyUtils.formatAmount(sel.totalAmount, currencySymbol), cx, cy + dpToPx(6), centerAmountPaint);
            String pctText = String.format(java.util.Locale.getDefault(), "%.1f%% of total", (sel.totalAmount / totalAmount) * 100f);
            canvas.drawText(pctText, cx, cy + dpToPx(24), centerSubtitlePaint);
        } else {
            canvas.drawText("Total Spent", cx, cy - dpToPx(12), centerTitlePaint);
            canvas.drawText(CurrencyUtils.formatAmount(totalAmount, currencySymbol), cx, cy + dpToPx(12), centerAmountPaint);
            canvas.drawText(dataList.size() + " Categories", cx, cy + dpToPx(30), centerSubtitlePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            double distance = Math.hypot(x - cx, y - cy);
            float radius = Math.min(cx, cy) - dpToPx(16);
            float innerRadius = radius * 0.62f;

            if (distance >= innerRadius && distance <= radius + dpToPx(10)) {
                // Compute angle
                double angleDeg = Math.toDegrees(Math.atan2(y - cy, x - cx));
                // Normalize angle to start at -90 deg
                float normalizedAngle = (float) ((angleDeg + 360 + 90) % 360);

                int clickedIndex = -1;
                for (int i = 0; i < sweepAngles.size(); i++) {
                    float start = (startAngles.get(i) + 90 + 360) % 360;
                    float sweep = sweepAngles.get(i);
                    float end = start + sweep;

                    if (end <= 360) {
                        if (normalizedAngle >= start && normalizedAngle < end) {
                            clickedIndex = i;
                            break;
                        }
                    } else {
                        if (normalizedAngle >= start || normalizedAngle < (end % 360)) {
                            clickedIndex = i;
                            break;
                        }
                    }
                }

                if (clickedIndex != -1) {
                    selectedIndex = (selectedIndex == clickedIndex) ? -1 : clickedIndex;
                    invalidate();
                    if (onCategorySelectedListener != null) {
                        onCategorySelectedListener.onCategorySelected(selectedIndex >= 0 ? dataList.get(selectedIndex) : null);
                    }
                    return true;
                }
            } else if (distance < innerRadius) {
                // Tapped center: reset selection
                if (selectedIndex != -1) {
                    selectedIndex = -1;
                    invalidate();
                    if (onCategorySelectedListener != null) {
                        onCategorySelectedListener.onCategorySelected(null);
                    }
                    return true;
                }
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
