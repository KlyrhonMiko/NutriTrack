package com.example.calorie_tracker.ui.food;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Drawable deleteIcon;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Paint clearPaint;
    private final OnSwipeListener onSwipeListener;

    public interface OnSwipeListener {
        void onSwiped(int position);
    }

    public SwipeToDeleteCallback(Context context, OnSwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.onSwipeListener = listener;

        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);

        backgroundColor = Color.parseColor("#F44336"); // Red
        background = new ColorDrawable(backgroundColor);

        clearPaint = new Paint();
        clearPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder, 
                         @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            onSwipeListener.onSwiped(position);
        }
    }
    
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, 
                           @NonNull RecyclerView.ViewHolder viewHolder, 
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        
        View itemView = viewHolder.itemView;

        if (dX == 0f && !isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        if (dX > 0) {
            background.setBounds(itemView.getLeft(), itemView.getTop(), 
                                itemView.getLeft() + ((int) dX), itemView.getBottom());
            background.draw(c);

            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        } else if (dX < 0) {
            background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(),
                                itemView.getRight(), itemView.getBottom());
            background.draw(c);

            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconRight = itemView.getRight() - iconMargin;
            int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
} 