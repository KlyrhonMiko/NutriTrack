package com.example.calorie_tracker.ui.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;

import java.util.ArrayList;
import java.util.List;

public class MealCategoryAdapter extends RecyclerView.Adapter<MealCategoryAdapter.MealCategoryViewHolder> {

    private List<MealCategory> mealCategories;
    private final OnFoodItemClickListener foodItemClickListener;
    private final OnDeleteMealEntryListener deleteListener;
    private final OnAddFoodClickListener addFoodListener;
    private final DiaryViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public interface OnFoodItemClickListener {
        void onFoodItemClick(MealEntry mealEntry, Food food);
    }

    public interface OnDeleteMealEntryListener {
        void onDeleteMealEntry(MealEntry mealEntry);
    }
    
    public interface OnAddFoodClickListener {
        void onAddFoodClick(String mealType);
    }

    public MealCategoryAdapter(List<MealCategory> mealCategories, 
                              DiaryViewModel viewModel,
                              LifecycleOwner lifecycleOwner,
                              OnFoodItemClickListener foodItemClickListener,
                              OnDeleteMealEntryListener deleteListener) {
        this.mealCategories = mealCategories;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.foodItemClickListener = foodItemClickListener;
        this.deleteListener = deleteListener;
        this.addFoodListener = null;
    }
    
    public MealCategoryAdapter(List<MealCategory> mealCategories, 
                              DiaryViewModel viewModel,
                              LifecycleOwner lifecycleOwner,
                              OnFoodItemClickListener foodItemClickListener,
                              OnDeleteMealEntryListener deleteListener,
                              OnAddFoodClickListener addFoodListener) {
        this.mealCategories = mealCategories;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.foodItemClickListener = foodItemClickListener;
        this.deleteListener = deleteListener;
        this.addFoodListener = addFoodListener;
    }

    @NonNull
    @Override
    public MealCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_category, parent, false);
        return new MealCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealCategoryViewHolder holder, int position) {
        MealCategory mealCategory = mealCategories.get(position);
        holder.bind(mealCategory);
    }

    @Override
    public int getItemCount() {
        return mealCategories.size();
    }

    public void updateMealCategories(List<MealCategory> newMealCategories) {
        this.mealCategories = newMealCategories;
        notifyDataSetChanged();
    }

    class MealCategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView mealTypeTextView;
        private final TextView mealCaloriesTextView;
        private final LinearLayout mealHeaderLayout;
        private final RecyclerView foodItemsRecyclerView;
        private final Context context;
        private MealFoodAdapter foodAdapter;
        private final com.google.android.material.button.MaterialButton addFoodButton;

        public MealCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mealTypeTextView = itemView.findViewById(R.id.mealTypeTextView);
            mealCaloriesTextView = itemView.findViewById(R.id.mealCaloriesTextView);
            mealHeaderLayout = itemView.findViewById(R.id.mealHeaderLayout);
            foodItemsRecyclerView = itemView.findViewById(R.id.foodItemsRecyclerView);
            addFoodButton = itemView.findViewById(R.id.addFoodButton);

            foodAdapter = new MealFoodAdapter(
                (mealEntry, food) -> {
                    if (foodItemClickListener != null) {
                        foodItemClickListener.onFoodItemClick(mealEntry, food);
                    }
                }
            );
            
            foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            foodItemsRecyclerView.setAdapter(foodAdapter);
            foodItemsRecyclerView.setNestedScrollingEnabled(false);
        }

        public void bind(MealCategory mealCategory) {
            mealTypeTextView.setText(mealCategory.getDisplayName());

            TextView timeTextView = itemView.findViewById(R.id.mealTimeTextView);
            timeTextView.setText(mealCategory.getTimeRange());

            mealCaloriesTextView.setText(String.valueOf(mealCategory.getTotalCalories()));

            int colorResId = context.getResources().getIdentifier(
                    mealCategory.getColorResourceName(), "color", context.getPackageName());
            
            if (colorResId != 0) {
                mealHeaderLayout.setBackgroundColor(context.getResources().getColor(colorResId));
            }

            String buttonText = "Add " + mealCategory.getDisplayName();
            addFoodButton.setText(buttonText);

            addFoodButton.setOnClickListener(v -> {
                if (addFoodListener != null) {
                    addFoodListener.onAddFoodClick(mealCategory.getMealType().toLowerCase());
                }
            });

            updateFoodItems(mealCategory);
        }
        
        private void updateFoodItems(MealCategory mealCategory) {
            List<MealEntry> entries = mealCategory.getEntries();
            
            if (entries.isEmpty()) {
                foodItemsRecyclerView.setVisibility(View.GONE);
            } else {
                foodItemsRecyclerView.setVisibility(View.VISIBLE);

                List<MealFoodAdapter.MealFoodItem> foodItems = new ArrayList<>();

                final int[] loadedItems = {0};
                final int totalItems = entries.size();

                for (MealEntry entry : entries) {
                    LiveData<Food> foodLiveData = viewModel.getFoodById(entry.getFoodId());

                    foodLiveData.removeObservers(lifecycleOwner);

                    foodLiveData.observe(lifecycleOwner, food -> {
                        if (food != null) {
                            MealFoodAdapter.MealFoodItem foodItem = 
                                    new MealFoodAdapter.MealFoodItem(entry, food);

                            foodItems.add(foodItem);

                            loadedItems[0]++;

                            if (loadedItems[0] == totalItems) {
                                foodAdapter.setItems(foodItems);
                            }
                        }
                    });
                }
            }
        }
    }
} 