package com.example.calorie_tracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.calorie_tracker.data.AppDatabase;
import com.example.calorie_tracker.data.dao.MealEntryDao;
import com.example.calorie_tracker.data.model.MealEntry;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MealEntryRepository {
    private final MealEntryDao mealEntryDao;

    public MealEntryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mealEntryDao = db.mealEntryDao();
    }

    public LiveData<MealEntry> getMealEntryById(long id) {
        return mealEntryDao.getMealEntryById(id);
    }

    public LiveData<List<MealEntry>> getAllMealEntriesForUser(long userId) {
        return mealEntryDao.getAllMealEntriesForUser(userId);
    }

    public LiveData<List<MealEntry>> getMealEntriesByDateRange(long userId, Date startDate, Date endDate) {
        return mealEntryDao.getMealEntriesByDateRange(userId, startDate, endDate);
    }

    public LiveData<List<MealEntry>> getMealEntriesByDate(long userId, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getMealEntriesByDate(userId, startOfDay, endOfDay);
    }

    public LiveData<List<MealEntry>> getMealEntriesByMealType(long userId, String mealType, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getMealEntriesByMealType(userId, mealType, startOfDay, endOfDay);
    }

    public LiveData<Integer> getTotalCaloriesForDay(long userId, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getTotalCaloriesForDay(userId, startOfDay, endOfDay);
    }
    
    public LiveData<Float> getTotalProteinForDay(long userId, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getTotalProteinForDay(userId, startOfDay, endOfDay);
    }
    
    public LiveData<Float> getTotalCarbsForDay(long userId, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getTotalCarbsForDay(userId, startOfDay, endOfDay);
    }
    
    public LiveData<Float> getTotalFatForDay(long userId, Date startOfDay, Date endOfDay) {
        return mealEntryDao.getTotalFatForDay(userId, startOfDay, endOfDay);
    }

    public void insert(MealEntry mealEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long generatedId = mealEntryDao.insert(mealEntry);
            mealEntry.setId(generatedId);
            
            System.out.println("DEBUG: MealEntry inserted with generated ID: " + generatedId);
        });
    }

    public void update(MealEntry mealEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (mealEntry.getId() > 0) {
                System.out.println("DEBUG: Updating meal entry in database with ID: " + mealEntry.getId() + 
                                 ", Food ID: " + mealEntry.getFoodId() + 
                                 ", Meal Type: " + mealEntry.getMealType() + 
                                 ", Servings: " + mealEntry.getServingsConsumed() + 
                                 ", Calories: " + mealEntry.getCaloriesConsumed());
                
                try {
                    int rowsUpdated = mealEntryDao.update(mealEntry);
                    System.out.println("DEBUG: Updated meal entry, rows affected: " + rowsUpdated);
                } catch (Exception e) {
                    System.out.println("DEBUG: Error updating meal entry: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: Cannot update meal entry with invalid ID: " + mealEntry.getId());
            }
        });
    }

    public void delete(MealEntry mealEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (mealEntry.getId() > 0) {
                System.out.println("DEBUG: Deleting meal entry with ID: " + mealEntry.getId() + 
                                 ", Food ID: " + mealEntry.getFoodId() + 
                                 ", Meal Type: " + mealEntry.getMealType());
                mealEntryDao.delete(mealEntry);
            } else {
                System.out.println("DEBUG: Cannot delete meal entry with invalid ID: " + mealEntry.getId());
            }
        });
    }

    public MealEntry findExistingMealEntrySync(long userId, long foodId, String mealType, Date startOfDay, Date endOfDay) {
        return mealEntryDao.findExistingMealEntrySync(userId, foodId, mealType, startOfDay, endOfDay);
    }
    
    public LiveData<MealEntry> findExistingMealEntry(long userId, long foodId, String mealType, Date startOfDay, Date endOfDay) {
        return mealEntryDao.findExistingMealEntry(userId, foodId, mealType, startOfDay, endOfDay);
    }
    
    public void insertOrUpdateMealEntry(MealEntry mealEntry, Date startOfDay, Date endOfDay) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            System.out.println("DEBUG: Checking for existing meal entry with " +
                             "User ID: " + mealEntry.getUserId() + 
                             ", Food ID: " + mealEntry.getFoodId() + 
                             ", Meal Type: " + mealEntry.getMealType() + 
                             ", Date Range: " + startOfDay + " to " + endOfDay);

            MealEntry existingEntry = findExistingMealEntrySync(
                mealEntry.getUserId(), 
                mealEntry.getFoodId(), 
                mealEntry.getMealType(), 
                startOfDay, 
                endOfDay
            );
            
            if (existingEntry != null) {
                System.out.println("DEBUG: Found existing meal entry with ID: " + existingEntry.getId() + 
                                 ", updating serving size from " + existingEntry.getServingsConsumed() + 
                                 " to " + (existingEntry.getServingsConsumed() + mealEntry.getServingsConsumed()));

                float newServingSize = existingEntry.getServingsConsumed() + mealEntry.getServingsConsumed();
                existingEntry.setServingsConsumed(newServingSize);

                int newCalories = Math.round(mealEntry.getCaloriesConsumed() / mealEntry.getServingsConsumed() * newServingSize);
                existingEntry.setCaloriesConsumed(newCalories);

                mealEntryDao.update(existingEntry);
                System.out.println("DEBUG: Updated existing entry instead of creating duplicate");
            } else {
                System.out.println("DEBUG: No existing entry found, inserting new meal entry");
                long generatedId = mealEntryDao.insert(mealEntry);
                mealEntry.setId(generatedId);
                System.out.println("DEBUG: New meal entry inserted with ID: " + generatedId);
            }
        });
    }
} 