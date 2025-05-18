package com.example.calorie_tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calorie_tracker.data.model.MealEntry;
import com.example.calorie_tracker.data.model.Food;

import java.util.Date;
import java.util.List;

@Dao
public interface MealEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MealEntry mealEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(MealEntry mealEntry);

    @Delete
    void delete(MealEntry mealEntry);

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    LiveData<MealEntry> getMealEntryById(long id);

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    MealEntry getMealEntryByIdSync(long id);

    @Query("SELECT * FROM meal_entries WHERE userId = :userId")
    LiveData<List<MealEntry>> getAllMealEntriesForUser(long userId);

    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    LiveData<List<MealEntry>> getMealEntriesByDateRange(long userId, Date startDate, Date endDate);

    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay")
    LiveData<List<MealEntry>> getMealEntriesByDate(long userId, Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND mealType = :mealType AND date >= :startOfDay AND date < :endOfDay")
    LiveData<List<MealEntry>> getMealEntriesByMealType(long userId, String mealType, Date startOfDay, Date endOfDay);

    @Query("SELECT SUM(caloriesConsumed) FROM meal_entries WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay")
    LiveData<Integer> getTotalCaloriesForDay(long userId, Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND foodId = :foodId AND mealType = :mealType AND date >= :startOfDay AND date < :endOfDay LIMIT 1")
    LiveData<MealEntry> findExistingMealEntry(long userId, long foodId, String mealType, Date startOfDay, Date endOfDay);
    
    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND foodId = :foodId AND mealType = :mealType AND date >= :startOfDay AND date < :endOfDay LIMIT 1")
    MealEntry findExistingMealEntrySync(long userId, long foodId, String mealType, Date startOfDay, Date endOfDay);
    
    @Query("SELECT SUM(f.protein * m.servingsConsumed) FROM meal_entries m JOIN foods f ON m.foodId = f.id WHERE m.userId = :userId AND m.date >= :startOfDay AND m.date < :endOfDay")
    LiveData<Float> getTotalProteinForDay(long userId, Date startOfDay, Date endOfDay);
    
    @Query("SELECT SUM(f.carbs * m.servingsConsumed) FROM meal_entries m JOIN foods f ON m.foodId = f.id WHERE m.userId = :userId AND m.date >= :startOfDay AND m.date < :endOfDay")
    LiveData<Float> getTotalCarbsForDay(long userId, Date startOfDay, Date endOfDay);
    
    @Query("SELECT SUM(f.fat * m.servingsConsumed) FROM meal_entries m JOIN foods f ON m.foodId = f.id WHERE m.userId = :userId AND m.date >= :startOfDay AND m.date < :endOfDay")
    LiveData<Float> getTotalFatForDay(long userId, Date startOfDay, Date endOfDay);
} 