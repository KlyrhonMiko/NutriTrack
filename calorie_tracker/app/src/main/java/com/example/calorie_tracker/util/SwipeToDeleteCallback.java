package com.example.calorie_tracker.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    public interface OnSwipeListener {
        void onSwiped(int position);
    }

    private final ColorDrawable background;
    private final Paint clearPaint;
    private final int backgroundColor;
    private final Drawable deleteIcon;
    private final int iconMargin;
    private final OnSwipeListener swipeListener;

    public SwipeToDeleteCallback(Context context, OnSwipeListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.swipeListener = listener;
        background = new ColorDrawable();
        backgroundColor = Color.parseColor("#F44336"); // Red color
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        iconMargin = (int) context.getResources().getDimension(R.dimen.item_margin);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        swipeListener.onSwiped(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        if (dX == 0) {
            c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), clearPaint);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        background.setColor(backgroundColor);
        background.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        background.draw(c);

        if (deleteIcon != null) {
            int intrinsicWidth = deleteIcon.getIntrinsicWidth();
            int intrinsicHeight = deleteIcon.getIntrinsicHeight();

            int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int iconBottom = iconTop + intrinsicHeight;
            int iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
            int iconRight = itemView.getRight() - iconMargin;

            if (dX < -intrinsicWidth * 2) {
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.draw(c);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
} 