package com.example.calorie_tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calorie_tracker.data.model.Food;

import java.util.List;

@Dao
public interface FoodDao {
    @Insert
    long insert(Food food);

    @Update
    void update(Food food);

    @Delete
    void delete(Food food);

    @Query("DELETE FROM foods WHERE id = :foodId")
    void deleteById(long foodId);

    @Query("DELETE FROM sqlite_sequence WHERE name='foods' AND (SELECT COUNT(*) FROM foods) = 0")
    void resetFoodSequence();

    @Query("DELETE FROM foods WHERE id = :foodId")
    int forceDeleteById(long foodId);

    @Query("DELETE FROM foods WHERE id = :foodId")
    void hardDeleteById(long foodId);

    @Query("UPDATE sqlite_sequence SET seq = (SELECT COALESCE(MAX(id), 0) FROM foods) WHERE name = 'foods'")
    void updateFoodSequence();

    @Query("SELECT COUNT(*) FROM foods WHERE id = :foodId")
    int checkIfFoodExists(long foodId);

    @Query("SELECT COUNT(*) FROM foods")
    int countTotalFoods();

    @Query("SELECT * FROM foods WHERE id = :id")
    LiveData<Food> getFoodById(long id);

    @Query("SELECT * FROM foods")
    LiveData<List<Food>> getAllFoods();

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :searchQuery || '%' OR brand LIKE '%' || :searchQuery || '%'")
    LiveData<List<Food>> searchFoods(String searchQuery);

    @Query("SELECT * FROM foods WHERE isFavorite = 1")
    LiveData<List<Food>> getFavoriteFoods();

    @Query("UPDATE foods SET isFavorite = :isFavorite WHERE id = :foodId")
    void setFavorite(long foodId, boolean isFavorite);

    @Query("SELECT * FROM foods WHERE isRecipe = 1")
    LiveData<List<Food>> getRecipes();

    @Query("UPDATE foods SET isRecipe = :isRecipe WHERE id = :foodId")
    void setRecipe(long foodId, boolean isRecipe);
} 