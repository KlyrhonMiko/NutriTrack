package com.example.calorie_tracker.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;

import java.util.ArrayList;
import java.util.List;

public class MealFoodAdapter extends RecyclerView.Adapter<MealFoodAdapter.MealFoodViewHolder> {

    private final List<MealFoodItem> foodItems = new ArrayList<>();
    private final OnFoodItemClickListener clickListener;

    public interface OnFoodItemClickListener {
        void onFoodItemClick(MealEntry mealEntry, Food food);
    }

    public MealFoodAdapter(OnFoodItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MealFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_food, parent, false);
        return new MealFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealFoodViewHolder holder, int position) {
        MealFoodItem item = foodItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public void setItems(List<MealFoodItem> items) {
        foodItems.clear();
        if (items != null) {
            foodItems.addAll(items);
        }
        notifyDataSetChanged();
    }
    
    public void addItem(MealFoodItem item) {
        foodItems.add(item);
        notifyItemInserted(foodItems.size() - 1);
    }
    
    public void removeItem(MealEntry mealEntry) {
        for (int i = 0; i < foodItems.size(); i++) {
            if (foodItems.get(i).getMealEntry().getId() == mealEntry.getId()) {
                foodItems.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    class MealFoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView foodNameTextView;
        private final TextView foodDetailsTextView;
        private final TextView foodCaloriesTextView;
        private final TextView proteinTextView;
        private final TextView carbsTextView;
        private final TextView fatTextView;

        public MealFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodDetailsTextView = itemView.findViewById(R.id.foodDetailsTextView);
            foodCaloriesTextView = itemView.findViewById(R.id.foodCaloriesTextView);
            proteinTextView = itemView.findViewById(R.id.proteinTextView);
            carbsTextView = itemView.findViewById(R.id.carbsTextView);
            fatTextView = itemView.findViewById(R.id.fatTextView);
        }

        public void bind(MealFoodItem item) {
            MealEntry mealEntry = item.getMealEntry();
            Food food = item.getFood();
            
            foodNameTextView.setText(food.getName());

            String servingText = String.format("%.1f serving(s) (%.0fg)", 
                    mealEntry.getServingsConsumed(), 
                    food.getServingSize() * mealEntry.getServingsConsumed());
            foodDetailsTextView.setText(servingText);

            foodCaloriesTextView.setText(String.valueOf(mealEntry.getCaloriesConsumed()));

            float servings = mealEntry.getServingsConsumed();
            float protein = food.getProtein() * servings;
            float carbs = food.getCarbs() * servings;
            float fat = food.getFat() * servings;

            proteinTextView.setText(formatMacroValue(protein));
            carbsTextView.setText(formatMacroValue(carbs));
            fatTextView.setText(formatMacroValue(fat));

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onFoodItemClick(mealEntry, food);
                }
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
    
    public static class MealFoodItem {
        private final MealEntry mealEntry;
        private final Food food;
        
        public MealFoodItem(MealEntry mealEntry, Food food) {
            this.mealEntry = mealEntry;
            this.food = food;
        }
        
        public MealEntry getMealEntry() {
            return mealEntry;
        }
        
        public Food getFood() {
            return food;
        }
    }
} 