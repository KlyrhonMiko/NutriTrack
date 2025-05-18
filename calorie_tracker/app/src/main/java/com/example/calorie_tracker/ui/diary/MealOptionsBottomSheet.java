package com.example.calorie_tracker.ui.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class MealOptionsBottomSheet extends BottomSheetDialogFragment {
    private MealEntry mealEntry;
    private Food food;
    private OnMealOptionSelectedListener listener;

    public interface OnMealOptionSelectedListener {
        void onEditSelected(MealEntry mealEntry, Food food);
        void onDeleteSelected(MealEntry mealEntry);
    }

    public static MealOptionsBottomSheet newInstance(MealEntry mealEntry, Food food) {
        MealOptionsBottomSheet fragment = new MealOptionsBottomSheet();
        fragment.mealEntry = mealEntry;
        fragment.food = food;
        return fragment;
    }

    public void setOnMealOptionSelectedListener(OnMealOptionSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_meal_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView foodNameTitle = view.findViewById(R.id.foodNameTitle);
        TextView caloriesText = view.findViewById(R.id.caloriesText);
        TextView servingSizeText = view.findViewById(R.id.servingSizeText);
        TextView proteinValue = view.findViewById(R.id.proteinValue);
        TextView carbsValue = view.findViewById(R.id.carbsValue);
        TextView fatValue = view.findViewById(R.id.fatValue);
        MaterialButton editButton = view.findViewById(R.id.editButton);
        MaterialButton deleteButton = view.findViewById(R.id.deleteButton);

        foodNameTitle.setText(food.getName());
        caloriesText.setText(mealEntry.getCaloriesConsumed() + " cal");

        String servingText = String.format("%.1f serving(s) (%.0fg)", 
                mealEntry.getServingsConsumed(), 
                food.getServingSize() * mealEntry.getServingsConsumed());
        servingSizeText.setText(servingText);

        float servings = mealEntry.getServingsConsumed();
        float protein = food.getProtein() * servings;
        float carbs = food.getCarbs() * servings;
        float fat = food.getFat() * servings;

        proteinValue.setText(formatMacroValue(protein));
        carbsValue.setText(formatMacroValue(carbs));
        fatValue.setText(formatMacroValue(fat));

        editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSelected(mealEntry, food);
            }
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSelected(mealEntry);
            }
            dismiss();
        });
    }

    private String formatMacroValue(float value) {
        if (value < 10) {
            return String.format("%.1fg", value);
        } else if (value < 100) {
            return String.format("%.0fg", value);
        } else {
            return String.format("%dg", Math.round(value));
        }
    }
} 