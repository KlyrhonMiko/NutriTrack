package com.example.calorie_tracker.ui.diary;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

public class ServingSelectorDialog extends DialogFragment {

    private Food food;
    private String mealType;
    private float selectedServingSize = 1.0f;
    private ServingSelectorListener listener;
    private OnServingConfirmedListener directListener;
    private TextView foodNameTextView;
    private TextView foodDetailsTextView;
    private TextView servingSizeValueText;
    private Slider servingSlider;
    private TextView caloriesValue;
    private TextView macrosInfoTextView;
    private MaterialButton addButton;
    private MaterialButton cancelButton;
    private MaterialButton decreaseButton;
    private MaterialButton increaseButton;

    public interface ServingSelectorListener {
        void onServingConfirmed(Food food, String mealType, float servingSize);
    }

    public interface OnServingConfirmedListener {
        void onServingConfirmed(Food food, String mealType, float servingSize);
    }

    public static ServingSelectorDialog newInstance(Food food, String mealType) {
        ServingSelectorDialog dialog = new ServingSelectorDialog();
        Bundle args = new Bundle();

        args.putLong("food_id", food.getId());
        args.putString("food_name", food.getName());
        args.putString("food_brand", food.getBrand());
        args.putInt("food_calories", food.getCalories());
        args.putFloat("food_serving_size", food.getServingSize());
        args.putFloat("food_protein", food.getProtein());
        args.putFloat("food_carbs", food.getCarbs());
        args.putFloat("food_fat", food.getFat());
        args.putFloat("food_fiber", food.getFiber());
        args.putFloat("food_sugar", food.getSugar());
        args.putBoolean("food_favorite", food.isFavorite());
        args.putBoolean("food_recipe", food.isRecipe());
        args.putString("mealType", mealType);
        
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (getParentFragment() instanceof ServingSelectorListener) {
                listener = (ServingSelectorListener) getParentFragment();
            } else {
                listener = (ServingSelectorListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent must implement ServingSelectorListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_CalorieTracker_Dialog);
        
        if (getArguments() != null) {
            Bundle args = getArguments();
            String foodName = args.getString("food_name", "");
            String foodBrand = args.getString("food_brand", "");
            int calories = args.getInt("food_calories", 0);
            float servingSize = args.getFloat("food_serving_size", 100);
            float protein = args.getFloat("food_protein", 0);
            float carbs = args.getFloat("food_carbs", 0);
            float fat = args.getFloat("food_fat", 0);
            float fiber = args.getFloat("food_fiber", 0);
            float sugar = args.getFloat("food_suxgar", 0);

            food = new Food(foodName, foodBrand, calories, servingSize, protein, carbs, fat, fiber, sugar);
            food.setId(args.getLong("food_id"));
            food.setFavorite(args.getBoolean("food_favorite", false));
            food.setRecipe(args.getBoolean("food_recipe", false));
            
            mealType = args.getString("mealType", "breakfast");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_serving_selector, container, false);

        foodNameTextView = view.findViewById(R.id.foodNameTextView);
        foodDetailsTextView = view.findViewById(R.id.foodDetailsTextView);
        servingSizeValueText = view.findViewById(R.id.servingSizeValueText);
        servingSlider = view.findViewById(R.id.servingSlider);
        caloriesValue = view.findViewById(R.id.caloriesValue);
        macrosInfoTextView = view.findViewById(R.id.macrosInfoTextView);
        addButton = view.findViewById(R.id.addButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        decreaseButton = view.findViewById(R.id.decreaseButton);
        increaseButton = view.findViewById(R.id.increaseButton);

        setupFoodInfo();

        setupServingSizeControls();

        setupButtons();
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
    
    private void setupFoodInfo() {
        if (food == null) {
            dismiss();
            return;
        }
        
        foodNameTextView.setText(food.getName());

        String detailsText;
        if (food.getBrand() != null && !food.getBrand().isEmpty()) {
            detailsText = food.getBrand() + " • " + food.getServingSize() + "g per serving";
        } else {
            detailsText = food.getServingSize() + "g per serving";
        }
        foodDetailsTextView.setText(detailsText);
        
        updateNutritionInfo();
    }
    
    private void setupServingSizeControls() {
        updateServingSizeText();

        servingSlider.setValue(selectedServingSize);
        servingSlider.addOnChangeListener((slider, value, fromUser) -> {
            selectedServingSize = value;
            updateServingSizeText();
            updateNutritionInfo();
        });

        decreaseButton.setOnClickListener(v -> {
            if (selectedServingSize > 0.25f) {
                selectedServingSize = Math.max(0.25f, selectedServingSize - 0.25f);
                servingSlider.setValue(selectedServingSize);
                updateServingSizeText();
                updateNutritionInfo();
            }
        });
        
        increaseButton.setOnClickListener(v -> {
            if (selectedServingSize < 5.0f) {
                selectedServingSize = Math.min(5.0f, selectedServingSize + 0.25f);
                servingSlider.setValue(selectedServingSize);
                updateServingSizeText();
                updateNutritionInfo();
            }
        });
    }
    
    private void setupButtons() {
        addButton.setOnClickListener(v -> {
            if (directListener != null) {
                directListener.onServingConfirmed(food, mealType, selectedServingSize);
            } else if (listener != null) {
                listener.onServingConfirmed(food, mealType, selectedServingSize);
            }
            dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dismiss());
    }
    
    private void updateServingSizeText() {
        float totalGrams = food.getServingSize() * selectedServingSize;
        servingSizeValueText.setText(String.format("%.2f serving(s) (%.0fg)", selectedServingSize, totalGrams));
    }
    
    private void updateNutritionInfo() {
        int calories = Math.round(food.getCalories() * selectedServingSize);
        float protein = food.getProtein() * selectedServingSize;
        float carbs = food.getCarbs() * selectedServingSize;
        float fat = food.getFat() * selectedServingSize;

        caloriesValue.setText(String.valueOf(calories));
        macrosInfoTextView.setText(String.format("P: %.1fg • C: %.1fg • F: %.1fg", protein, carbs, fat));
    }

    public void setInitialServingSize(float servingSize) {
        this.selectedServingSize = servingSize;
        if (servingSlider != null) {
            servingSlider.setValue(servingSize);
            updateServingSizeText();
            updateNutritionInfo();
        }
    }

    public void setOnServingConfirmedListener(OnServingConfirmedListener listener) {
        this.directListener = listener;
    }
} 