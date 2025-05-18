package com.example.calorie_tracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.calorie_tracker.R;
import com.google.android.material.card.MaterialCardView;

public class CustomToast {

    // Toast types
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_INFO = 3;
    public static final int TYPE_WARNING = 4;

    public static void show(Context context, String message, int duration, int type) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);

        MaterialCardView cardView = view.findViewById(R.id.toastCardView);
        TextView textView = view.findViewById(R.id.toastTextView);
        ImageView iconView = view.findViewById(R.id.toastIconView);

        textView.setText(message);

        switch (type) {
            case TYPE_SUCCESS:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.success));
                iconView.setImageResource(R.drawable.ic_check_circle);
                break;
            case TYPE_ERROR:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error));
                iconView.setImageResource(R.drawable.ic_error);
                break;
            case TYPE_INFO:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                iconView.setImageResource(R.drawable.ic_info);
                break;
            case TYPE_WARNING:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.warning));
                iconView.setImageResource(R.drawable.ic_warning);
                break;
        }

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(view);
        toast.show();
    }

    public static void showSuccess(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, TYPE_SUCCESS);
    }

    public static void showError(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, TYPE_ERROR);
    }

    public static void showInfo(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, TYPE_INFO);
    }

    public static void showWarning(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, TYPE_WARNING);
    }
} 