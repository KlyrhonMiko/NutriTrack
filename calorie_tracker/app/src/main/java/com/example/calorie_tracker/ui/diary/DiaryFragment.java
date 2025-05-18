package com.example.calorie_tracker.ui.diary;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.MainActivity;
import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;

public class DiaryFragment extends Fragment implements ServingSelectorDialog.ServingSelectorListener {

    private DiaryViewModel viewModel;
    
    // UI Components
    private TextView dateTextView;
    private ImageButton previousDayButton;
    private ImageButton nextDayButton;
    private CircularProgressIndicator calorieProgress;
    private TextView caloriesRemainingText;
    private TextView goalCaloriesTextView;
    private TextView consumedCaloriesTextView;
    private TextView carbsValue;
    private TextView proteinValue;
    private TextView fatValue;
    private LinearProgressIndicator carbsProgress;
    private LinearProgressIndicator proteinProgress;
    private LinearProgressIndicator fatProgress;
    private RecyclerView mealsRecyclerView;
    private View emptyStateView;
    private ProgressBar loadingProgressBar;
    private TextView mealsHeaderTextView;
    
    private MealCategoryAdapter mealCategoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity(), 
                new DiaryViewModel.Factory(requireActivity().getApplication()))
                .get(DiaryViewModel.class);

        initializeViews(view);
        setupListeners();
        setupMealsRecyclerView();
        observeViewModel();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshUserData();
    }
    
    private void initializeViews(View view) {
        dateTextView = view.findViewById(R.id.dateTextView);
        previousDayButton = view.findViewById(R.id.previousDayButton);
        nextDayButton = view.findViewById(R.id.nextDayButton);
        
        calorieProgress = view.findViewById(R.id.calorieProgress);
        caloriesRemainingText = view.findViewById(R.id.caloriesRemainingText);
        goalCaloriesTextView = view.findViewById(R.id.goalCaloriesTextView);
        consumedCaloriesTextView = view.findViewById(R.id.consumedCaloriesTextView);
        
        carbsValue = view.findViewById(R.id.carbsValue);
        proteinValue = view.findViewById(R.id.proteinValue);
        fatValue = view.findViewById(R.id.fatValue);
        carbsProgress = view.findViewById(R.id.carbsProgress);
        proteinProgress = view.findViewById(R.id.proteinProgress);
        fatProgress = view.findViewById(R.id.fatProgress);
        
        mealsRecyclerView = view.findViewById(R.id.mealsRecyclerView);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        mealsHeaderTextView = view.findViewById(R.id.mealsHeaderTextView);
    }
    
    private void setupListeners() {
        previousDayButton.setOnClickListener(v -> {
            viewModel.moveToPreviousDay();
        });
        
        nextDayButton.setOnClickListener(v -> {
            viewModel.moveToNextDay();
        });

        dateTextView.setOnClickListener(v -> {
            showDatePickerDialog();
        });
    }
    
    private void setupMealsRecyclerView() {
        mealCategoryAdapter = new MealCategoryAdapter(
            new ArrayList<>(),
            viewModel,
            getViewLifecycleOwner(),
            (mealEntry, food) -> {
                MealOptionsBottomSheet bottomSheet = MealOptionsBottomSheet.newInstance(mealEntry, food);
                bottomSheet.setOnMealOptionSelectedListener(new MealOptionsBottomSheet.OnMealOptionSelectedListener() {
                    @Override
                    public void onEditSelected(MealEntry mealEntry, Food food) {
                        showServingSizeDialog(mealEntry, food);
                    }

                    @Override
                    public void onDeleteSelected(MealEntry mealEntry) {
                        new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Remove Food")
                            .setMessage("Remove " + mealEntry.getCaloriesConsumed() + " calories from your diary?")
                            .setPositiveButton("Remove", (dialog, which) -> {
                                viewModel.deleteMealEntry(mealEntry);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                });
                bottomSheet.show(getChildFragmentManager(), "MealOptionsBottomSheet");
            },
            mealEntry -> {},
            mealType -> {
                showAddFoodDialog(mealType);
            }
        );
        
        mealsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mealsRecyclerView.setAdapter(mealCategoryAdapter);
    }
    
    private void showAddFoodDialog(String mealType) {
        AddFoodDialogFragment dialogFragment = AddFoodDialogFragment.newInstance(mealType);
        dialogFragment.show(getChildFragmentManager(), "AddFoodDialog");
    }
    
    @Override
    public void onServingConfirmed(Food food, String mealType, float servingSize) {
        Date selectedDate = viewModel.getSelectedDate().getValue();

        System.out.println("DEBUG: DiaryFragment using selected date: " + selectedDate);

        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        selectedDate = calendar.getTime();
        
        int calories = Math.round(food.getCalories() * servingSize);

        MealEntry mealEntry = new MealEntry(
            1,
            food.getId(),
            selectedDate,
            mealType.toLowerCase(),
            servingSize,
            calories
        );

        viewModel.addMealEntry(mealEntry);

        Toast.makeText(requireContext(), food.getName() + " added to " + mealType, Toast.LENGTH_SHORT).show();
    }
    
    private void showServingSizeDialog(MealEntry mealEntry, Food food) {
        ServingSelectorDialog dialog = ServingSelectorDialog.newInstance(food, mealEntry.getMealType());
        dialog.setInitialServingSize(mealEntry.getServingsConsumed());
        dialog.setOnServingConfirmedListener((updatedFood, mealType, newServingSize) -> {
            updateMealEntry(mealEntry, food, newServingSize);
        });
        dialog.show(getChildFragmentManager(), "ServingSizeDialog");
    }
    
    private void updateMealEntry(MealEntry mealEntry, Food food, float newServingSize) {
        System.out.println("DEBUG: Updating meal entry with ID: " + mealEntry.getId());
        System.out.println("DEBUG: Before update - Meal type: " + mealEntry.getMealType() + 
                          ", Food ID: " + mealEntry.getFoodId() + 
                          ", Current serving size: " + mealEntry.getServingsConsumed() +
                          ", Date: " + mealEntry.getDate());
        
        if (mealEntry.getId() <= 0) {
            System.out.println("DEBUG: ERROR - Trying to update entry with invalid ID: " + mealEntry.getId());
            Toast.makeText(requireContext(), "Error updating entry", Toast.LENGTH_SHORT).show();
            return;
        }

        mealEntry.setServingsConsumed(newServingSize);
        mealEntry.setCaloriesConsumed(Math.round(food.getCalories() * newServingSize));

        System.out.println("DEBUG: After update - ID: " + mealEntry.getId() + 
                          ", New serving size: " + mealEntry.getServingsConsumed() + 
                          ", New calories: " + mealEntry.getCaloriesConsumed());

        viewModel.updateMealEntry(mealEntry);

        viewModel.refreshData();

        new Handler().postDelayed(() -> {
            System.out.println("DEBUG: Re-observing after update");
            observeViewModel();
        }, 250);

        Toast.makeText(requireContext(), "Serving size updated", Toast.LENGTH_SHORT).show();
    }
    
    private void observeViewModel() {
        viewModel.getFormattedDate().observe(getViewLifecycleOwner(), date -> {
            dateTextView.setText(date);
        });

        viewModel.getDailyCalorieGoal().observe(getViewLifecycleOwner(), goal -> {
            if (goal != null) {
                goalCaloriesTextView.setText(goal + " cal");
                updateCalorieProgress(goal, viewModel.getTotalCaloriesForSelectedDate().getValue());
            }
        });

        viewModel.getTotalCaloriesForSelectedDate().observe(getViewLifecycleOwner(), consumed -> {
            if (consumed != null) {
                consumedCaloriesTextView.setText(consumed + " cal");
                updateCalorieProgress(viewModel.getDailyCalorieGoal().getValue(), consumed);
            } else {
                consumedCaloriesTextView.setText("0 cal");
                updateCalorieProgress(viewModel.getDailyCalorieGoal().getValue(), 0);
            }
        });

        viewModel.getRemainingCalories().observe(getViewLifecycleOwner(), remaining -> {
            if (remaining != null) {
                caloriesRemainingText.setText(String.valueOf(remaining));
            }
        });

        MediatorLiveData<Map<String, Float>> combinedMacros = new MediatorLiveData<>();
        combinedMacros.addSource(viewModel.getTotalProteinForSelectedDate(), protein -> {
            Map<String, Float> current = combinedMacros.getValue();
            if (current == null) current = new HashMap<>();
            current.put("protein", protein);
            combinedMacros.setValue(current);
        });
        
        combinedMacros.addSource(viewModel.getTotalCarbsForSelectedDate(), carbs -> {
            Map<String, Float> current = combinedMacros.getValue();
            if (current == null) current = new HashMap<>();
            current.put("carbs", carbs);
            combinedMacros.setValue(current);
        });
        
        combinedMacros.addSource(viewModel.getTotalFatForSelectedDate(), fat -> {
            Map<String, Float> current = combinedMacros.getValue();
            if (current == null) current = new HashMap<>();
            current.put("fat", fat);
            combinedMacros.setValue(current);
        });

        combinedMacros.observe(getViewLifecycleOwner(), macros -> {
            if (macros != null && macros.size() == 3) {
                Float protein = macros.get("protein");
                Float carbs = macros.get("carbs");
                Float fat = macros.get("fat");

                protein = protein != null ? protein : 0f;
                carbs = carbs != null ? carbs : 0f;
                fat = fat != null ? fat : 0f;

                int proteinDisplay = Math.round(protein);
                int carbsDisplay = Math.round(carbs); 
                int fatDisplay = Math.round(fat);

                proteinValue.setText(proteinDisplay + "g");
                carbsValue.setText(carbsDisplay + "g");
                fatValue.setText(fatDisplay + "g");

                int proteinPercentage = Math.min(Math.round((protein / 50f) * 100), 100);
                int carbsPercentage = Math.min(Math.round((carbs / 275f) * 100), 100);
                int fatPercentage = Math.min(Math.round((fat / 70f) * 100), 100);

                proteinProgress.setProgress(proteinPercentage);
                carbsProgress.setProgress(carbsPercentage);
                fatProgress.setProgress(fatPercentage);
                
                System.out.println("DEBUG: Updated macronutrients - Protein: " + protein + "g, Carbs: " + carbs + "g, Fat: " + fat + "g");
            }
        });

        viewModel.getMealEntriesGroupedByType().observe(getViewLifecycleOwner(), mealEntriesByType -> {
            if (mealEntriesByType != null) {
                updateMealCategories(mealEntriesByType);

                boolean hasEntries = false;
                for (List<MealEntry> entries : mealEntriesByType.values()) {
                    if (!entries.isEmpty()) {
                        hasEntries = true;
                        break;
                    }
                }
                
                updateVisibilityState(hasEntries);
            } else {
                updateVisibilityState(false);
            }
        });
    }
    
    private void updateVisibilityState(boolean hasEntries) {
        mealsRecyclerView.setVisibility(View.VISIBLE);

        emptyStateView.setVisibility(View.GONE);

        mealsHeaderTextView.setVisibility(View.VISIBLE);

        loadingProgressBar.setVisibility(View.GONE);
    }
    
    private void updateCalorieProgress(Integer goal, Integer consumed) {
        if (goal == null || goal <= 0) {
            goal = 2000;
        }
        
        if (consumed == null) {
            consumed = 0;
        }

        int percentage = (int) (((float) consumed / goal) * 100);
        calorieProgress.setProgress(percentage);

        int remaining = goal - consumed;
        caloriesRemainingText.setText(String.valueOf(remaining));
    }
    
    private void updateMealCategories(Map<String, List<MealEntry>> mealEntriesByType) {
        List<MealCategory> mealCategories = new ArrayList<>();

        String[] mealTypes = {"breakfast", "lunch", "dinner", "snack"};

        for (String mealType : mealTypes) {
            List<MealEntry> entries = mealEntriesByType.getOrDefault(mealType, new ArrayList<>());

            int totalCalories = 0;
            for (MealEntry entry : entries) {
                totalCalories += entry.getCaloriesConsumed();
            }

            MealCategory category = new MealCategory(mealType, totalCalories, entries);
            mealCategories.add(category);
        }

        mealCategoryAdapter.updateMealCategories(mealCategories);
    }

    private void showDatePickerDialog() {
        Date currentDate = viewModel.getSelectedDate().getValue();
        if (currentDate == null) {
            currentDate = new Date();
        }

        com.google.android.material.datepicker.MaterialDatePicker.Builder<Long> builder =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker();
        
        builder.setTitleText("Select Date");
        builder.setTheme(R.style.Theme_CalorieTracker_MaterialDatePicker);
        builder.setSelection(currentDate.getTime());

        com.google.android.material.datepicker.CalendarConstraints.Builder constraintsBuilder =
                new com.google.android.material.datepicker.CalendarConstraints.Builder();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        
        constraintsBuilder.setEnd(today.getTimeInMillis());
        constraintsBuilder.setValidator(
                com.google.android.material.datepicker.DateValidatorPointBackward.now());
        
        builder.setCalendarConstraints(constraintsBuilder.build());
        
        com.google.android.material.datepicker.MaterialDatePicker<Long> picker = builder.build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            Date newSelectedDate = calendar.getTime();
            
            viewModel.setSelectedDate(newSelectedDate);
        });
        
        picker.show(getChildFragmentManager(), picker.toString());
    }
} 