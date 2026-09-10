package com.expensetracker.monthly.ui.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    public interface OnSwipeListener {
        void onSwiped(int position);
    }

    private final OnSwipeListener listener;
    private final Drawable deleteIcon;
    private final int iconMargin;
    private final Paint backgroundPaint;
    private final float cornerRadius;

    public SwipeToDeleteCallback(Context context, OnSwipeListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;

        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        if (deleteIcon != null) {
            deleteIcon.setTint(Color.WHITE);
        }

        iconMargin = (int) (16 * context.getResources().getDisplayMetrics().density);
        cornerRadius = 12 * context.getResources().getDisplayMetrics().density;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.swipe_delete_bg));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && listener != null) {
            listener.onSwiped(pos);
        }
    }

    @Override
    public void onChildDraw(
            @NonNull Canvas c,
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            float dX,
            float dY,
            int actionState,
            boolean isCurrentlyActive
    ) {
        View itemView = viewHolder.itemView;

        if (dX < 0) { // Swiping left
            RectF background = new RectF(
                    itemView.getRight() + dX,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
            c.drawRoundRect(background, cornerRadius, cornerRadius, backgroundPaint);

            if (deleteIcon != null) {
                int iconSize = (int) (24 * recyclerView.getContext().getResources().getDisplayMetrics().density);
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int iconTop = itemView.getTop() + (itemHeight - iconSize) / 2;
                int iconBottom = iconTop + iconSize;
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - iconSize;

                if (itemView.getRight() + dX < iconLeft) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
