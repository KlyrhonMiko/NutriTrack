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
            // Set the ID back on the item
            foodItem.setId(id);
        });
    }

    /**
     * Insert a food item and block until completed
     * @param foodItem The food item to insert
     * @return The database ID of the inserted item
     */
    public long insertSync(FoodItem foodItem) {
        // Create a CompletableFuture to wait for the result
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Food food = convertToFood(foodItem);
                long id = foodDao.insert(food);
                // Set the ID back on the item
                foodItem.setId(id);
                future.complete(id);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        try {
            // Wait for the insertion to complete and return the ID
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return -1 to indicate failure
        }
    }

    public void update(FoodItem foodItem, long foodId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Food food = convertToFood(foodItem);
            food.setId(foodId);
            foodDao.update(food);
        });
    }

    /**
     * Delete a food item from the database by its ID
     * @param foodId ID of the food item to delete
     */
    public void delete(long foodId) {
        // Queue up deletion on a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Log the deletion attempt
                System.out.println("DELETION: Starting food deletion process for ID: " + foodId);
                
                // Step 1: Attempt all normal deletion approaches
                boolean deletionSuccessful = false;
                
                try {
                    // First try hard delete method
                    foodDao.hardDeleteById(foodId);
                    
                    // Update sequence tables
                    foodDao.updateFoodSequence();
                    
                    // Also try standard methods as fallbacks
                    foodDao.deleteById(foodId);
                    int rowsDeleted = foodDao.forceDeleteById(foodId);
                    
                    System.out.println("DELETION: Initial deletion attempts completed, rows deleted: " + rowsDeleted);
                    
                    // Create a minimal Food object with just the ID for entity-based deletion as last resort
                    Food food = new Food("", "", 0, 0, 0, 0, 0, 0, 0);
                    food.setId(foodId);
            foodDao.delete(food);
                } catch (Exception e) {
                    System.err.println("DELETION: Error in initial deletion attempts: " + e.getMessage());
                }
                
                // Step 2: Verify if food was actually deleted
                int exists = foodDao.checkIfFoodExists(foodId);
                System.out.println("DELETION: Verification - ID: " + foodId + " - Still exists: " + (exists > 0));
                
                // Step 3: If food still exists, try more aggressive deletion
                if (exists > 0) {
                    System.out.println("DELETION: Food still exists! Trying aggressive methods...");
                    
                    try {
                        // Try more forceful deletion with direct SQL
                        foodDao.forceDeleteById(foodId);
                    } catch (Exception e) {
                        System.err.println("DELETION: Error in aggressive deletion: " + e.getMessage());
                    }
                    
                    // Sleep briefly to allow database changes to settle
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        // Ignore
                    }
                    
                    // Check again after aggressive deletion
                    exists = foodDao.checkIfFoodExists(foodId);
                    System.out.println("DELETION: After aggressive deletion - ID: " + foodId + " - Still exists: " + (exists > 0));
                    
                    // Step 4: Final attempt with direct SQL execution if still exists
                    if (exists > 0) {
                        try {
                            // Direct SQL execution through AppDatabase
                            AppDatabase db = AppDatabase.getDatabase(null);
                            db.getOpenHelper().getWritableDatabase()
                                .execSQL("DELETE FROM foods WHERE id = ?", new Object[]{foodId});
                            System.out.println("DELETION: Performed direct SQL DELETE as last resort");
                            
                            // Verify again
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
                
                // Step 5: Database maintenance if deletion was successful
                if (deletionSuccessful) {
                    System.out.println("DELETION: Successfully deleted food item #" + foodId);
                    
                    // Reset sequence if needed
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
                // Log any errors that might occur
                System.err.println("DELETION: Critical error deleting food item: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Note: Database optimization through VACUUM or PRAGMA commands
     * is not directly supported by Room's @Query annotation.
     * These would require the use of SupportSQLiteDatabase directly.
     */

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

    /**
     * Create a Food entity from a FoodItem and save it to the database
     * This is useful when working with temporary FoodItem objects that need to be persisted
     * @param foodItem The FoodItem to convert
     * @param callback Callback with the newly created food ID
     */
    public void createFoodFromFoodItem(FoodItem foodItem, FoodViewModel.FoodCreatedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Convert FoodItem to Food entity
                Food food = new Food(
                    foodItem.getName(),
                    foodItem.getBrand(),
                    foodItem.getCalories(),
                    100f, // Default serving size in grams
                    (float)foodItem.getProtein(),
                    (float)foodItem.getCarbs(),
                    (float)foodItem.getFat(),
                    0f, // Default fiber
                    0f  // Default sugar
                );
                
                // Set favorite and recipe status
                food.setFavorite(foodItem.isFavorite());
                food.setRecipe(foodItem.isRecipe());
                
                // Insert into database and get the generated ID
                long id = foodDao.insert(food);
                
                // Log the creation for debugging
                System.out.println("Created new Food entity with ID: " + id + 
                                  ", Name: " + food.getName());
                
                // Execute callback on the main thread
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
                    "Other", // Food entity doesn't have category, defaulting to "Other"
                    food.getServingSize() + "g",
                    food.getCalories(),
                    food.getProtein(),
                    food.getCarbs(),
                    food.getFat(),
                    food.isFavorite(),
                    true,  // All foods from DB are considered user-created for this app
                    food.isRecipe() // Set the recipe flag from the Food entity
            );
            // Set the ID from the database entity to ensure it matches
            item.setId(food.getId());
            foodItems.add(item);
        }
        return foodItems;
    }

    private Food convertToFood(FoodItem item) {
        // Parse the serving string to get the numeric value
        String servingStr = item.getServing().replaceAll("[^\\d.]", "");
        float servingSize = 100f; // Default value
        try {
            servingSize = Float.parseFloat(servingStr);
        } catch (NumberFormatException e) {
            // Use default value if parsing fails
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