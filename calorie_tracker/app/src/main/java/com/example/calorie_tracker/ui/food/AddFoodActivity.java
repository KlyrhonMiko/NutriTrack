package com.example.calorie_tracker.ui.food;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.repository.FoodRepository;
import com.example.calorie_tracker.util.CustomToast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddFoodActivity extends AppCompatActivity {
    
    private TextInputEditText nameEditText;
    private TextInputEditText brandEditText;
    private TextInputEditText servingEditText;
    private TextInputEditText caloriesEditText;
    private TextInputEditText proteinEditText;
    private TextInputEditText carbsEditText;
    private TextInputEditText fatEditText;
    private ToggleButton favoriteToggle;
    private ToggleButton addToRecipeToggle;
    private TextView recipeToggleStatus;
    private MaterialButton saveButton;
    
    private FoodRepository foodRepository;
    private boolean isEditMode = false;
    private long editFoodId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        foodRepository = new FoodRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();

        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);
        if (isEditMode) {
            editFoodId = getIntent().getLongExtra("food_id", -1);
            String name = getIntent().getStringExtra("food_name");
            String brand = getIntent().getStringExtra("food_brand");
            String serving = getIntent().getStringExtra("food_serving");
            int calories = getIntent().getIntExtra("food_calories", 0);
            double protein = getIntent().getDoubleExtra("food_protein", 0);
            double carbs = getIntent().getDoubleExtra("food_carbs", 0);
            double fat = getIntent().getDoubleExtra("food_fat", 0);
            boolean isFavorite = getIntent().getBooleanExtra("food_favorite", false);
            boolean isRecipe = getIntent().getBooleanExtra("food_recipe", false);

            nameEditText.setText(name);
            brandEditText.setText(brand);
            servingEditText.setText(serving);
            caloriesEditText.setText(String.valueOf(calories));
            proteinEditText.setText(String.valueOf(protein));
            carbsEditText.setText(String.valueOf(carbs));
            fatEditText.setText(String.valueOf(fat));
            favoriteToggle.setChecked(isFavorite);
            addToRecipeToggle.setChecked(isRecipe);

            setTitle("Edit Food");
            saveButton.setText("Update");
        }

        saveButton.setOnClickListener(v -> validateAndSaveFood());

        addToRecipeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recipeToggleStatus.setText(isChecked ? "Added to My Recipes" : "Not added to recipes");
            recipeToggleStatus.setTextColor(getResources().getColor(isChecked ? R.color.primary : R.color.text_secondary));
        });

        View recipeHelpView = findViewById(R.id.recipeHelpButton);
        if (recipeHelpView != null) {
            recipeHelpView.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                    .setTitle("My Recipes")
                    .setMessage("Add this food to My Recipes to easily find it later when creating meal plans or tracking your food.")
                    .setPositiveButton("Got it", null)
                    .show();
            });
        }
    }
    
    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        brandEditText = findViewById(R.id.brandEditText);
        servingEditText = findViewById(R.id.servingEditText);
        caloriesEditText = findViewById(R.id.caloriesEditText);
        proteinEditText = findViewById(R.id.proteinEditText);
        carbsEditText = findViewById(R.id.carbsEditText);
        fatEditText = findViewById(R.id.fatEditText);
        favoriteToggle = findViewById(R.id.favoriteToggle);
        addToRecipeToggle = findViewById(R.id.addToRecipeToggle);
        recipeToggleStatus = findViewById(R.id.recipeToggleStatus);
        saveButton = findViewById(R.id.saveButton);
    }
    
    private void validateAndSaveFood() {
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Food name is required");
            return;
        }
        
        if (TextUtils.isEmpty(servingEditText.getText())) {
            servingEditText.setError("Serving size is required");
            return;
        }
        
        if (TextUtils.isEmpty(caloriesEditText.getText())) {
            caloriesEditText.setError("Calories are required");
            return;
        }

        String name = nameEditText.getText().toString().trim();
        String brand = brandEditText.getText().toString().trim();
        String serving = servingEditText.getText().toString().trim();
        
        int calories;
        double protein = 0, carbs = 0, fat = 0;
        
        try {
            calories = Integer.parseInt(caloriesEditText.getText().toString().trim());
            
            if (!TextUtils.isEmpty(proteinEditText.getText())) {
                protein = Double.parseDouble(proteinEditText.getText().toString().trim());
            }
            
            if (!TextUtils.isEmpty(carbsEditText.getText())) {
                carbs = Double.parseDouble(carbsEditText.getText().toString().trim());
            }
            
            if (!TextUtils.isEmpty(fatEditText.getText())) {
                fat = Double.parseDouble(fatEditText.getText().toString().trim());
            }
        } catch (NumberFormatException e) {
            CustomToast.showError(this, "Please enter valid numbers for nutritional values");
            return;
        }
        
        boolean isFavorite = favoriteToggle.isChecked();
        boolean isAddedToRecipes = addToRecipeToggle.isChecked();

        FoodItem foodItem = new FoodItem(
            name, 
            brand, 
            "Other",
            serving, 
            calories, 
            protein, 
            carbs, 
            fat, 
            isFavorite, 
            true,
            isAddedToRecipes
        );
        
        if (isEditMode && editFoodId != -1) {
            foodRepository.update(foodItem, editFoodId);
        } else {
            foodRepository.insert(foodItem);
        }

        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 