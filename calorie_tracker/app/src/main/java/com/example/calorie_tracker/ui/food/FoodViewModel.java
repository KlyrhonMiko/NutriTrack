package com.example.calorie_tracker.ui.food;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.repository.FoodRepository;

import java.util.List;

public class FoodViewModel extends AndroidViewModel {
    private final FoodRepository repository;
    private final LiveData<List<FoodItem>> allFoods;
    private final LiveData<List<FoodItem>> favoriteFoods;
    private final LiveData<List<FoodItem>> recipes;
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public FoodViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodRepository(application);
        allFoods = repository.getAllFoods();
        favoriteFoods = repository.getFavoriteFoods();
        recipes = repository.getRecipes();
    }

    public LiveData<List<FoodItem>> getAllFoods() {
        return allFoods;
    }

    public LiveData<List<FoodItem>> getFavoriteFoods() {
        return favoriteFoods;
    }
    
    public LiveData<List<FoodItem>> getRecipes() {
        return recipes;
    }

    public LiveData<List<FoodItem>> getSearchResults() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query.isEmpty()) {
                return allFoods;
            } else {
                return repository.searchFoods(query);
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insertFood(FoodItem food) {
        repository.insert(food);
    }

    public long insertFoodSync(FoodItem food) {
        return repository.insertSync(food);
    }

    public void setFavorite(long foodId, boolean isFavorite) {
        repository.setFavorite(foodId, isFavorite);
    }
    
    public void setRecipe(long foodId, boolean isRecipe) {
        repository.setRecipe(foodId, isRecipe);
    }

    public LiveData<Food> getFoodById(long foodId) {
        return repository.getFoodById(foodId);
    }
    

    public void createFoodFromFoodItem(FoodItem foodItem, FoodCreatedCallback callback) {
        repository.createFoodFromFoodItem(foodItem, callback);
    }

    public interface FoodCreatedCallback {
        void onFoodCreated(long foodId);
    }

    public void deleteFood(long foodId) {
        repository.delete(foodId);

        System.out.println("FoodViewModel: Requesting deletion of food ID: " + foodId);

        if (searchQuery.getValue() != null && !searchQuery.getValue().isEmpty()) {
            String currentQuery = searchQuery.getValue();
            searchQuery.setValue(null);
            searchQuery.setValue(currentQuery);
        }
    }
} 