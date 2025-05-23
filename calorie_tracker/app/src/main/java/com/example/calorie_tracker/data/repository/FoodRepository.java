package com.example.calorie_tracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.calorie_tracker.data.AppDatabase;
import com.example.calorie_tracker.data.dao.FoodDao;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.ui.food.FoodItem;
import com.example.calorie_tracker.ui.food.FoodViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FoodRepository {
    private final FoodDao foodDao;

    public FoodRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        foodDao = db.foodDao();
    }

    public void insert(FoodItem foodItem) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Food food = convertToFood(foodItem);
            long id = foodDao.insert(food);
            foodItem.setId(id);
        });
    }
    public long insertSync(FoodItem foodItem) {
        CompletableFuture<Long> future = new CompletableFuture<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Food food = convertToFood(foodItem);
                long id = foodDao.insert(food);
                foodItem.setId(id);
                future.complete(id);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void update(FoodItem foodItem, long foodId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Food food = convertToFood(foodItem);
            food.setId(foodId);
            foodDao.update(food);
        });
    }

    public void delete(long foodId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                System.out.println("DELETION: Starting food deletion process for ID: " + foodId);

                boolean deletionSuccessful = false;

                try {
                    foodDao.hardDeleteById(foodId);

                    foodDao.updateFoodSequence();

                    foodDao.deleteById(foodId);
                    int rowsDeleted = foodDao.forceDeleteById(foodId);

                    System.out.println("DELETION: Initial deletion attempts completed, rows deleted: " + rowsDeleted);

                    Food food = new Food("", "", 0, 0, 0, 0, 0, 0, 0);
                    food.setId(foodId);
            foodDao.delete(food);
                } catch (Exception e) {
                    System.err.println("DELETION: Error in initial deletion attempts: " + e.getMessage());
                }

                int exists = foodDao.checkIfFoodExists(foodId);
                System.out.println("DELETION: Verification - ID: " + foodId + " - Still exists: " + (exists > 0));

                if (exists > 0) {
                    System.out.println("DELETION: Food still exists! Trying aggressive methods...");

                    try {
                        foodDao.forceDeleteById(foodId);
                    } catch (Exception e) {
                        System.err.println("DELETION: Error in aggressive deletion: " + e.getMessage());
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                    }

                    exists = foodDao.checkIfFoodExists(foodId);
                    System.out.println("DELETION: After aggressive deletion - ID: " + foodId + " - Still exists: " + (exists > 0));

                    if (exists > 0) {
                        try {
                            AppDatabase db = AppDatabase.getDatabase(null);
                            db.getOpenHelper().getWritableDatabase()
                                .execSQL("DELETE FROM foods WHERE id = ?", new Object[]{foodId});
                            System.out.println("DELETION: Performed direct SQL DELETE as last resort");

                            exists = foodDao.checkIfFoodExists(foodId);
                            deletionSuccessful = (exists == 0);
                        } catch (Exception e) {
                            System.err.println("DELETION: Error in direct SQL execution: " + e.getMessage());
                        }
                    } else {
                        deletionSuccessful = true;
                    }
                } else {
                    deletionSuccessful = true;
                }

                if (deletionSuccessful) {
                    System.out.println("DELETION: Successfully deleted food item #" + foodId);

                    try {
                        int totalFoods = foodDao.countTotalFoods();
                        if (totalFoods == 0) {
                            foodDao.resetFoodSequence();
                            System.out.println("DELETION: Reset food sequence after deletion");
                        }
                    } catch (Exception e) {
                        System.err.println("DELETION: Error resetting sequence: " + e.getMessage());
                    }
                } else {
                    System.err.println("DELETION: WARNING - Failed to delete food item #" + foodId + " after multiple attempts!");
                }
            } catch (Exception e) {
                System.err.println("DELETION: Critical error deleting food item: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void setFavorite(long foodId, boolean isFavorite) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodDao.setFavorite(foodId, isFavorite);
        });
    }

    public LiveData<List<FoodItem>> getAllFoods() {
        return Transformations.map(foodDao.getAllFoods(), this::convertToFoodItems);
    }

    public LiveData<List<FoodItem>> searchFoods(String query) {
        return Transformations.map(foodDao.searchFoods(query), this::convertToFoodItems);
    }

    public LiveData<List<FoodItem>> getFavoriteFoods() {
        return Transformations.map(foodDao.getFavoriteFoods(), this::convertToFoodItems);
    }

    public void setRecipe(long foodId, boolean isRecipe) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodDao.setRecipe(foodId, isRecipe);
        });
    }

    public void createFoodFromFoodItem(FoodItem foodItem, FoodViewModel.FoodCreatedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Food food = new Food(
                    foodItem.getName(),
                    foodItem.getBrand(),
                    foodItem.getCalories(),
                    100f,
                    (float)foodItem.getProtein(),
                    (float)foodItem.getCarbs(),
                    (float)foodItem.getFat(),
                    0f,
                    0f
                );

                food.setFavorite(foodItem.isFavorite());
                food.setRecipe(foodItem.isRecipe());

                long id = foodDao.insert(food);

                System.out.println("Created new Food entity with ID: " + id +
                                  ", Name: " + food.getName());

                AppDatabase.getDatabase(null).getQueryExecutor().execute(() -> {
                    callback.onFoodCreated(id);
                });
            } catch (Exception e) {
                System.err.println("Error creating Food from FoodItem: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public LiveData<List<FoodItem>> getRecipes() {
        return Transformations.map(foodDao.getRecipes(), this::convertToFoodItems);
    }

    public LiveData<Food> getFoodById(long foodId) {
        return foodDao.getFoodById(foodId);
    }

    private List<FoodItem> convertToFoodItems(List<Food> foods) {
        List<FoodItem> foodItems = new ArrayList<>();
        for (Food food : foods) {
            FoodItem item = new FoodItem(
                    food.getName(),
                    food.getBrand(),
                    "Other",
                    food.getServingSize() + "g",
                    food.getCalories(),
                    food.getProtein(),
                    food.getCarbs(),
                    food.getFat(),
                    food.isFavorite(),
                    true,
                    food.isRecipe()
            );
            item.setId(food.getId());
            foodItems.add(item);
        }
        return foodItems;
    }

    private Food convertToFood(FoodItem item) {
        String servingStr = item.getServing().replaceAll("[^\\d.]", "");
        float servingSize = 100f;
        try {
            servingSize = Float.parseFloat(servingStr);
        } catch (NumberFormatException e) {
        }

        Food food = new Food(
                item.getName(),
                item.getBrand(),
                item.getCalories(),
                servingSize,
                (float) item.getProtein(),
                (float) item.getCarbs(),
                (float) item.getFat(),
                0f,
                0f
        );
        food.setFavorite(item.isFavorite());
        food.setRecipe(item.isRecipe());
        return food;
    }

    public LiveData<List<Food>> getAllFoodEntities() {
        return foodDao.getAllFoods();
    }

    public LiveData<List<Food>> searchFoodsByName(String query) {
        return foodDao.searchFoods("%" + query + "%");
    }

    public LiveData<List<FoodItem>> getAllFoodItems() {
        return Transformations.map(foodDao.getAllFoods(), this::convertToFoodItems);
    }
}