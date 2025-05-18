package com.example.calorie_tracker.ui.diary;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddFoodDialogFragment extends DialogFragment implements ServingSelectorDialog.ServingSelectorListener {

    private DiaryViewModel diaryViewModel;
    private FoodSearchAdapter foodAdapter;
    private RecyclerView foodsRecyclerView;
    private EditText searchEditText;
    private ProgressBar loadingIndicator;
    private View emptyState;
    private String mealType;
    private TextView mealTypeTitle;

    public static AddFoodDialogFragment newInstance(String mealType) {
        AddFoodDialogFragment fragment = new AddFoodDialogFragment();
        Bundle args = new Bundle();
        args.putString("mealType", mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_CalorieTracker_Dialog_FullScreen);
        
        if (getArguments() != null) {
            mealType = getArguments().getString("mealType", "breakfast");
        }

        diaryViewModel = new ViewModelProvider(requireActivity()).get(DiaryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_food, container, false);

        mealTypeTitle = view.findViewById(R.id.mealTypeTitle);
        searchEditText = view.findViewById(R.id.searchEditText);
        foodsRecyclerView = view.findViewById(R.id.foodsRecyclerView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        emptyState = view.findViewById(R.id.emptyStateView);
        MaterialButton closeButton = view.findViewById(R.id.closeButton);
        View confirmationCard = view.findViewById(R.id.confirmationCard);

        String formattedMealType = mealType.substring(0, 1).toUpperCase() + mealType.substring(1);
        mealTypeTitle.setText("Add to " + formattedMealType);

        foodsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        foodAdapter = new FoodSearchAdapter(new ArrayList<>(), food -> {
            // When a food is clicked, show the serving selector dialog
            showServingSelectorDialog(food);
        });
        foodsRecyclerView.setAdapter(foodAdapter);

        setupSearch();

        closeButton.setOnClickListener(v -> dismiss());

        loadFoods();
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                window.setStatusBarColor(getResources().getColor(R.color.primary));

                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                window.getDecorView().setSystemUiVisibility(flags);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.layoutInDisplayCutoutMode = 
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    window.setAttributes(layoutParams);
                }
            }
        }
    }
    
    private void loadFoods() {
        showLoading(true);
        diaryViewModel.getAllFoods().observe(getViewLifecycleOwner(), foods -> {
            showLoading(false);
            updateFoodsList(foods);
        });
    }
    
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        loadFoods();
                    } else {
                        performSearch(query);
                    }
                };
                
                handler.postDelayed(searchRunnable, 300); // 300ms debounce
            }
        });
    }
    
    private void performSearch(String query) {
        showLoading(true);
        diaryViewModel.searchFoods(query).observe(getViewLifecycleOwner(), foods -> {
            showLoading(false);
            updateFoodsList(foods);
        });
    }
    
    private void updateFoodsList(List<Food> foods) {
        if (foods.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            foodAdapter.updateFoods(foods);
        }
    }
    
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        foodsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }
    
    private void showEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        foodsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void showServingSelectorDialog(Food food) {
        ServingSelectorDialog servingSelectorDialog = ServingSelectorDialog.newInstance(food, mealType);
        servingSelectorDialog.show(getChildFragmentManager(), "ServingSelectorDialog");
    }
    
    private void showConfirmationMessage(String message) {
        View confirmationCard = getView().findViewById(R.id.confirmationCard);
        TextView confirmationText = getView().findViewById(R.id.confirmationText);
        
        confirmationText.setText(message);
        confirmationCard.setVisibility(View.VISIBLE);
        confirmationCard.setAlpha(0f);
        confirmationCard.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction(() -> {
                new Handler().postDelayed(() -> {
                    confirmationCard.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            confirmationCard.setVisibility(View.GONE);
                            dismiss();
                        })
                        .start();
                }, 1500); // Show for 1.5 seconds
            })
            .start();
    }
    
    @Override
    public void onServingConfirmed(Food food, String mealType, float servingSize) {
        Date selectedDate = diaryViewModel.getSelectedDate().getValue();
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Error: No date selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date normalizedDate = calendar.getTime();

        int totalCalories = Math.round(food.getCalories() * servingSize);

        MealEntry mealEntry = new MealEntry(
            1L,
            food.getId(),
            normalizedDate,
            mealType,
            servingSize,
            totalCalories
        );

        diaryViewModel.addMealEntry(mealEntry);

        String message = String.format("%s added to %s", food.getName(), mealType);
        showConfirmationMessage(message);
    }

    private static class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.FoodViewHolder> {
        
        private List<Food> foods;
        private final OnFoodClickListener listener;
        
        public interface OnFoodClickListener {
            void onFoodClick(Food food);
        }
        
        public FoodSearchAdapter(List<Food> foods, OnFoodClickListener listener) {
            this.foods = foods;
            this.listener = listener;
        }
        
        public void updateFoods(List<Food> newFoods) {
            this.foods = newFoods;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_food, parent, false);
            return new FoodViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            Food food = foods.get(position);
            holder.bind(food);
        }
        
        @Override
        public int getItemCount() {
            return foods.size();
        }
        
        class FoodViewHolder extends RecyclerView.ViewHolder {
            private final TextView nameTextView;
            private final TextView brandTextView;
            private final TextView caloriesTextView;
            private final TextView macrosTextView;
            
            public FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.foodNameTextView);
                brandTextView = itemView.findViewById(R.id.foodDetailsTextView);
                caloriesTextView = itemView.findViewById(R.id.foodCaloriesTextView);
                macrosTextView = brandTextView;

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFoodClick(foods.get(position));
                    }
                });
            }
            
            public void bind(Food food) {
                nameTextView.setText(food.getName());

                String macrosInfo = String.format("P: %.1fg • C: %.1fg • F: %.1fg", 
                    food.getProtein(), food.getCarbs(), food.getFat());

                if (food.getBrand() != null && !food.getBrand().isEmpty()) {
                    brandTextView.setText(food.getBrand() + " • " + macrosInfo);
                } else {
                    brandTextView.setText(macrosInfo);
                }

                caloriesTextView.setText(String.valueOf(food.getCalories()));
            }
        }
    }
} 