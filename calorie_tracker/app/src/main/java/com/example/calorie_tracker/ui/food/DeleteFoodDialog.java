package com.example.calorie_tracker.ui.food;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DeleteFoodDialog {

    public interface DeleteFoodListener {
        void onFoodDeleted(FoodItem foodItem);
    }
    
    public static void show(@NonNull Context context, @NonNull FoodItem foodItem, 
                            @NonNull DeleteFoodListener listener) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Delete Food")
            .setMessage("Are you sure you want to delete " + foodItem.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                listener.onFoodDeleted(foodItem);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 