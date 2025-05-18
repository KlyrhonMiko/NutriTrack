package com.example.calorie_tracker.ui.food;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.MainActivity;
import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.AppDatabase;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.example.calorie_tracker.data.repository.FoodRepository;
import com.example.calorie_tracker.data.repository.MealEntryRepository;
import com.example.calorie_tracker.ui.diary.DiaryViewModel;
import com.example.calorie_tracker.util.CustomToast;
import com.example.calorie_tracker.util.SwipeToDeleteCallback;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class FoodFragment extends Fragment {

    private RecyclerView foodsRecyclerView;
    private SearchView searchView;
    private TabLayout tabLayout;
    private View emptyStateView;
    private ProgressBar loadingProgressBar;
    private ExtendedFloatingActionButton addFoodFab;
    
    private FoodViewModel foodViewModel;
    private DiaryViewModel diaryViewModel;
    private List<FoodItem> allFoods = new ArrayList<>();
    private FoodAdapter foodAdapter;
    private int currentTabPosition = 0;
    private FoodItem selectedFood;

    private String mealType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            mealType = getArguments().getString("mealType");
        }

        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        diaryViewModel = new ViewModelProvider(requireActivity()).get(DiaryViewModel.class);

        System.out.println("DEBUG: FoodFragment created, DiaryViewModel selected date: " + 
                          (diaryViewModel.getSelectedDate().getValue() != null ? 
                           diaryViewModel.getSelectedDate().getValue() : "null"));

        foodsRecyclerView = view.findViewById(R.id.foodsRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        tabLayout = view.findViewById(R.id.tabLayout);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        addFoodFab = view.findViewById(R.id.addFoodFab);

        foodAdapter = new FoodAdapter(allFoods, food -> {
            if (mealType != null) {
                showServingSelectionDialog(food);
            } else {
                onFoodItemClicked(food);
            }
        });
        
        foodAdapter.setFavoriteListener(this::onFoodFavoriteChanged);
        foodAdapter.setRecipeListener(this::onFoodRecipeChanged);
        foodAdapter.setLongClickListener(this::onFoodItemLongClicked);
        foodsRecyclerView.setAdapter(foodAdapter);
        foodsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupSwipeToDelete();

        foodViewModel.getAllFoods().observe(getViewLifecycleOwner(), foods -> {
            allFoods = foods;
            applyCurrentFilters();
        });

        setupSearch();

        setupTabs();

        addFoodFab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddFoodActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.emptyStateActionButton).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddFoodActivity.class);
            startActivity(intent);
        });

        if (mealType != null) {
            updateTitleForMealType();
        }

        updateUIState();
    }
    
    private void updateTitleForMealType() {
        String formattedMealType = mealType.substring(0, 1).toUpperCase() + mealType.substring(1);
        getActivity().setTitle("Add to " + formattedMealType);
    }
    
    private void showServingSelectionDialog(FoodItem food) {
        try {
            System.out.println("Food selected: " + (food != null ? food.getName() : "null"));

            if (getActivity() != null) {
                final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
                
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Add to which meal?")
                    .setItems(mealTypes, (dialog, which) -> {
                        String selectedMealType = mealTypes[which].toLowerCase();

                        Date selectedDate = diaryViewModel.getSelectedDate().getValue();

                        System.out.println("DEBUG: FoodFragment using selected date: " + selectedDate);

                        if (selectedDate == null) {
                            selectedDate = new Date();
                            System.out.println("WARNING: No selected date available in DiaryViewModel, using today as fallback");
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(selectedDate);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        selectedDate = calendar.getTime();

                        System.out.println("DEBUG: Creating MealEntry from direct click");
                        System.out.println("DEBUG: Selected date from DiaryViewModel: " + selectedDate);
                        System.out.println("DEBUG: Food ID: " + food.getId());
                        System.out.println("DEBUG: Meal type: " + selectedMealType);
                        System.out.println("DEBUG: Food calories: " + food.getCalories());

                        MealEntry mealEntry = new MealEntry(
                            1L,
                            food.getId(),
                            selectedDate,
                            selectedMealType,
                            1.0f,
                            food.getCalories()
                        );

                        diaryViewModel.addMealEntry(mealEntry);

                        if (getActivity() instanceof MainActivity) {
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.popBackStack();

                            ((MainActivity) getActivity()).loadDiaryFragment();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        } catch (Exception e) {
            System.out.println("Error showing dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showServingSizeDialogWithMealType(FoodItem food, String mealType) {
        try {
            System.out.println("Selected food for meal: " + (food != null ? food.getName() : "null"));

            if (mealType != null && food != null) {
                try {
                    Date selectedDate = diaryViewModel.getSelectedDate().getValue();

                    System.out.println("DEBUG: FoodFragment using selected date in meal type dialog: " + selectedDate);

                    if (selectedDate == null) {
                        selectedDate = new Date();
                        System.out.println("WARNING: No selected date available in DiaryViewModel, using today as fallback");
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(selectedDate);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    selectedDate = calendar.getTime();

                    System.out.println("DEBUG: Creating/updating MealEntry with specific meal type: " + mealType);
                    System.out.println("DEBUG: For date: " + selectedDate);
                    System.out.println("DEBUG: Food ID: " + food.getId());
                    System.out.println("DEBUG: Food calories: " + food.getCalories());

                    MealEntry mealEntry = new MealEntry(
                        1L,
                        food.getId(),
                        selectedDate,
                        mealType,
                        1.0f,
                        food.getCalories()
                    );

                    diaryViewModel.addMealEntry(mealEntry);

                    if (getActivity() instanceof MainActivity) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();

                        ((MainActivity) getActivity()).loadDiaryFragment();
                    }
                } catch (Exception e) {
                    System.out.println("Error adding meal: " + e.getMessage());
                    e.printStackTrace();

                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), 
                            "Failed to add food: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in showServingSizeDialogWithMealType: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFoods(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFoods(newText);
                return true;
            }
        });
    }
    
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                applyCurrentFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    
    private void applyCurrentFilters() {
        String query = searchView.getQuery().toString();

        List<FoodItem> filteredByTab = new ArrayList<>();
        switch (currentTabPosition) {
            case 0:
                filteredByTab.addAll(allFoods);
                break;
            case 1:
                for (FoodItem food : allFoods) {
                    if (food.isFavorite()) {
                        filteredByTab.add(food);
                    }
                }
                break;
            case 2:
                for (FoodItem food : allFoods) {
                    if (food.isRecipe()) {
                        filteredByTab.add(food);
                    }
                }
                break;
        }

        List<FoodItem> finalFiltered = new ArrayList<>();
        if (query.isEmpty()) {
            finalFiltered.addAll(filteredByTab);
        } else {
            for (FoodItem food : filteredByTab) {
                if (food.getName().toLowerCase().contains(query.toLowerCase()) ||
                    food.getBrand().toLowerCase().contains(query.toLowerCase())) {
                    finalFiltered.add(food);
                }
            }
        }
        
        foodAdapter.updateFoods(finalFiltered);
        updateUIState();
    }
    
    private void filterFoods(String query) {
        applyCurrentFilters();
    }
    
    private void onFoodItemClicked(FoodItem foodItem) {
        if (foodItem == null) return;
        
        try {
            System.out.println("Food item clicked: " + foodItem.getName());

            Intent intent = new Intent(requireContext(), AddFoodActivity.class);

            intent.putExtra("food_id", foodItem.getId());
            intent.putExtra("food_name", foodItem.getName());
            intent.putExtra("food_brand", foodItem.getBrand());
            intent.putExtra("food_serving", foodItem.getServing());
            intent.putExtra("food_calories", foodItem.getCalories());
            intent.putExtra("food_protein", foodItem.getProtein());
            intent.putExtra("food_carbs", foodItem.getCarbs());
            intent.putExtra("food_fat", foodItem.getFat());
            intent.putExtra("food_favorite", foodItem.isFavorite());
            intent.putExtra("food_recipe", foodItem.isRecipe());
            intent.putExtra("is_edit_mode", true);

            startActivity(intent);
            
        } catch (Exception e) {
            System.out.println("Error in onFoodItemClicked: " + e.getMessage());
            e.printStackTrace();

            if (getActivity() != null) {
                Toast.makeText(getActivity(), 
                    "Failed to edit food: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void onFoodFavoriteChanged(FoodItem foodItem, boolean isFavorite) {
        foodViewModel.setFavorite(foodItem.getId(), isFavorite);
    }
    
    private void onFoodRecipeChanged(FoodItem foodItem, boolean isRecipe) {
        foodViewModel.setRecipe(foodItem.getId(), isRecipe);

        if (currentTabPosition == 2 && !isRecipe) {
            applyCurrentFilters();
        }
    }
    
    private void onFoodItemLongClicked(FoodItem foodItem, View itemView) {
        selectedFood = foodItem;

        DeleteFoodDialog.show(requireContext(), foodItem, this::deleteFoodFromDatabase);
    }
    
    private void deleteFoodFromDatabase(FoodItem foodItem) {
        final FoodItem foodToDelete = new FoodItem(
            foodItem.getName(),
            foodItem.getBrand(),
            foodItem.getCategory(),
            foodItem.getServing(),
            foodItem.getCalories(),
            foodItem.getProtein(),
            foodItem.getCarbs(),
            foodItem.getFat(),
            foodItem.isFavorite(),
            foodItem.isRecipe()
        );
        foodToDelete.setId(foodItem.getId());

        System.out.println("FoodFragment: Deleting food with ID: " + foodToDelete.getId() + ", Name: " + foodToDelete.getName());

        int position = -1;
        for (int i = 0; i < foodAdapter.getItemCount(); i++) {
            FoodItem item = foodAdapter.getItemAt(i);
            if (item != null && item.getId() == foodItem.getId()) {
                position = i;
                break;
            }
        }

        final int finalPosition = position;

        if (finalPosition != -1) {
            foodAdapter.removeItem(finalPosition);
        }

        foodViewModel.deleteFood(foodToDelete.getId());

        View rootView = getView();
        if (rootView != null) {
            Snackbar snackbar = Snackbar.make(rootView, foodToDelete.getName() + " deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> {
                FoodItem newFoodItem = new FoodItem(
                    foodToDelete.getName(),
                    foodToDelete.getBrand(),
                    foodToDelete.getCategory(),
                    foodToDelete.getServing(),
                    foodToDelete.getCalories(),
                    foodToDelete.getProtein(),
                    foodToDelete.getCarbs(),
                    foodToDelete.getFat(),
                    foodToDelete.isFavorite(),
                    foodToDelete.isRecipe()
                );

                long newId = foodViewModel.insertFoodSync(newFoodItem);

                System.out.println("FoodFragment: Undoing delete - created new food with ID: " + newId + ", Name: " + newFoodItem.getName());

                if (shouldItemBeVisible(newFoodItem)) {
                    if (finalPosition != -1 && finalPosition < foodAdapter.getItemCount()) {
                        foodAdapter.insertItem(finalPosition, newFoodItem);
                    } else {
                        foodAdapter.addItem(newFoodItem);
                    }
                }
            });
            snackbar.show();
        }

        updateUIState();
    }
    
    private void setupSwipeToDelete() {
        SwipeToDeleteCallback swipeCallback = new SwipeToDeleteCallback(
                requireContext(),
                position -> {
                    FoodItem swipedFood = foodAdapter.getItemAt(position);
                    if (swipedFood != null) {
                        final FoodItem foodCopy = new FoodItem(
                            swipedFood.getName(),
                            swipedFood.getBrand(),
                            swipedFood.getCategory(),
                            swipedFood.getServing(),
                            swipedFood.getCalories(),
                            swipedFood.getProtein(),
                            swipedFood.getCarbs(),
                            swipedFood.getFat(),
                            swipedFood.isFavorite(),
                            swipedFood.isRecipe()
                        );
                        foodCopy.setId(swipedFood.getId());

                        System.out.println("FoodFragment: Swipe-deleting food with ID: " + foodCopy.getId() + ", Name: " + foodCopy.getName());

                        final int swipePosition = position;

                        foodAdapter.removeItem(position);

                        foodViewModel.deleteFood(foodCopy.getId());

                        View rootView = getView();
                        if (rootView != null) {
                            Snackbar snackbar = Snackbar.make(rootView, foodCopy.getName() + " deleted", Snackbar.LENGTH_LONG);
                            snackbar.setAction("UNDO", v -> {
                                FoodItem newFoodItem = new FoodItem(
                                    foodCopy.getName(),
                                    foodCopy.getBrand(),
                                    foodCopy.getCategory(),
                                    foodCopy.getServing(),
                                    foodCopy.getCalories(),
                                    foodCopy.getProtein(),
                                    foodCopy.getCarbs(),
                                    foodCopy.getFat(),
                                    foodCopy.isFavorite(),
                                    foodCopy.isRecipe()
                                );

                                long newId = foodViewModel.insertFoodSync(newFoodItem);

                                System.out.println("FoodFragment: Undoing swipe-delete - created new food with ID: " + newId + ", Name: " + newFoodItem.getName());

                                if (shouldItemBeVisible(newFoodItem)) {
                                    if (swipePosition < foodAdapter.getItemCount()) {
                                        foodAdapter.insertItem(swipePosition, newFoodItem);
                                    } else {
                                        foodAdapter.addItem(newFoodItem);
                                    }
                                }
                            });
                            snackbar.show();
                        }
                        updateUIState();
                    }
                }
        );

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(foodsRecyclerView);
    }

    private boolean shouldItemBeVisible(FoodItem item) {
        switch (currentTabPosition) {
            case 1:
                if (!item.isFavorite()) {
                    return false;
                }
                break;
            case 2:
                if (!item.isRecipe()) {
                    return false;
                }
                break;
        }

        String query = searchView.getQuery().toString().toLowerCase();
        if (!query.isEmpty()) {
            return item.getName().toLowerCase().contains(query) || 
                   item.getBrand().toLowerCase().contains(query);
        }

        return true;
    }
    
    private void updateUIState() {
        if (foodAdapter.getItemCount() == 0) {
            foodsRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            foodsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
        
        loadingProgressBar.setVisibility(View.GONE);
    }

    private Date createNormalizedToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (diaryViewModel != null) {
            diaryViewModel.refreshData();
            
            System.out.println("DEBUG: FoodFragment onResume, selected date: " + 
                              (diaryViewModel.getSelectedDate().getValue() != null ? 
                               diaryViewModel.getSelectedDate().getValue() : "null"));
        }
    }
} 