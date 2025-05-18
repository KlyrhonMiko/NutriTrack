package com.example.calorie_tracker.ui.food;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodItems;
    private final OnFoodItemClickListener listener;
    private OnFoodFavoriteListener favoriteListener;
    private OnFoodRecipeListener recipeListener;
    private OnFoodItemLongClickListener longClickListener;

    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
    }

    public interface OnFoodFavoriteListener {
        void onFoodFavoriteChanged(FoodItem foodItem, boolean isFavorite);
    }
    
    public interface OnFoodRecipeListener {
        void onFoodRecipeChanged(FoodItem foodItem, boolean isRecipe);
    }
    
    public interface OnFoodItemLongClickListener {
        void onFoodItemLongClick(FoodItem foodItem, View itemView);
    }

    public FoodAdapter(List<FoodItem> foodItems, OnFoodItemClickListener listener) {
        this.foodItems = new ArrayList<>(foodItems);
        this.listener = listener;
    }

    public void setFavoriteListener(OnFoodFavoriteListener favoriteListener) {
        this.favoriteListener = favoriteListener;
    }
    
    public void setRecipeListener(OnFoodRecipeListener recipeListener) {
        this.recipeListener = recipeListener;
    }
    
    public void setLongClickListener(OnFoodItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void updateFoods(List<FoodItem> newFoods) {
        this.foodItems = new ArrayList<>(newFoods);
        notifyDataSetChanged();
    }

    public void setItems(List<FoodItem> newItems) {
        this.foodItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public FoodItem getItemAt(int position) {
        if (position >= 0 && position < foodItems.size()) {
            return foodItems.get(position);
        }
        return null;
    }

    public FoodItem removeItem(int position) {
        if (position >= 0 && position < foodItems.size()) {
            FoodItem removedItem = foodItems.remove(position);
            notifyItemRemoved(position);
            return removedItem;
        }
        return null;
    }

    public void insertItem(int position, FoodItem item) {
        if (position >= 0 && position <= foodItems.size()) {
            foodItems.add(position, item);
            notifyItemInserted(position);
        }
    }

    public void addItem(FoodItem item) {
        foodItems.add(item);
        notifyItemInserted(foodItems.size() - 1);
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_detail, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem food = foodItems.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView foodNameTextView;
        private final TextView foodBrandTextView;
        private final TextView foodServingTextView;
        private final TextView foodCaloriesTextView;
        private final TextView proteinTextView;
        private final TextView carbsTextView;
        private final TextView fatTextView;
        private final CheckBox favoriteButton;
        private final CheckBox recipeButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodBrandTextView = itemView.findViewById(R.id.foodBrandTextView);
            foodServingTextView = itemView.findViewById(R.id.foodServingTextView);
            foodCaloriesTextView = itemView.findViewById(R.id.foodCaloriesTextView);
            proteinTextView = itemView.findViewById(R.id.proteinTextView);
            carbsTextView = itemView.findViewById(R.id.carbsTextView);
            fatTextView = itemView.findViewById(R.id.fatTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            recipeButton = itemView.findViewById(R.id.recipeButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFoodItemClick(foodItems.get(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onFoodItemLongClick(foodItems.get(position), itemView);
                    return true; // consume the long click
                }
                return false;
            });
        }

        public void bind(FoodItem food) {
            foodNameTextView.setText(food.getName());
            foodBrandTextView.setText(food.getBrand());
            foodServingTextView.setText("Serving: " + food.getServing());
            foodCaloriesTextView.setText(String.valueOf(food.getCalories()));
            proteinTextView.setText(String.format("%.1fg", food.getProtein()));
            carbsTextView.setText(String.format("%.1fg", food.getCarbs()));
            fatTextView.setText(String.format("%.1fg", food.getFat()));

            favoriteButton.setChecked(food.isFavorite());

            recipeButton.setChecked(food.isRecipe());

            favoriteButton.setOnClickListener(v -> {
                boolean isChecked = favoriteButton.isChecked();
                food.setFavorite(isChecked);

                if (favoriteListener != null) {
                    favoriteListener.onFoodFavoriteChanged(food, isChecked);
                }
            });

            recipeButton.setOnClickListener(v -> {
                boolean isChecked = recipeButton.isChecked();
                food.setRecipe(isChecked);

                if (recipeListener != null) {
                    recipeListener.onFoodRecipeChanged(food, isChecked);
                }
            });
        }
    }
} 