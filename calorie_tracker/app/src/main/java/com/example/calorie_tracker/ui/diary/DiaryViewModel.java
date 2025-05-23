package com.example.calorie_tracker.ui.diary;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.example.calorie_tracker.data.repository.FoodRepository;
import com.example.calorie_tracker.data.repository.MealEntryRepository;
import com.example.calorie_tracker.data.repository.UserRepository;
import com.example.calorie_tracker.data.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DiaryViewModel extends AndroidViewModel {
    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<Long> currentUserId = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

    private final LiveData<List<MealEntry>> mealEntriesForSelectedDate;
    private final LiveData<Integer> totalCaloriesForSelectedDate;
    private final LiveData<Integer> dailyCalorieGoal;
    private final LiveData<Float> totalProteinForSelectedDate;
    private final LiveData<Float> totalCarbsForSelectedDate;
    private final LiveData<Float> totalFatForSelectedDate;

    public DiaryViewModel(@NonNull Application application) {
        super(application);
        mealEntryRepository = new MealEntryRepository(application);
        foodRepository = new FoodRepository(application);
        userRepository = new UserRepository(application);

        if (selectedDate.getValue() == null && !selectedDate.hasActiveObservers()) {
            selectedDate.setValue(new Date());
            System.out.println("DEBUG: DiaryViewModel initialized with today's date");
        }

        currentUserId.setValue(1L);

        mealEntriesForSelectedDate = Transformations.switchMap(
                selectedDate, date -> Transformations.switchMap(
                        currentUserId, userId -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = calendar.getTime();
                            
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            Date endOfDay = calendar.getTime();
                            
                            System.out.println("DEBUG: Querying entries from " + startOfDay + " to " + endOfDay);
                            return mealEntryRepository.getMealEntriesByDate(userId, startOfDay, endOfDay);
                        }
                )
        );

        totalCaloriesForSelectedDate = Transformations.switchMap(
                selectedDate, date -> Transformations.switchMap(
                        currentUserId, userId -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = calendar.getTime();
                            
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            Date endOfDay = calendar.getTime();
                            
                            return mealEntryRepository.getTotalCaloriesForDay(userId, startOfDay, endOfDay);
                        }
                )
        );

        dailyCalorieGoal = Transformations.switchMap(
                currentUserId, userId -> userRepository.getUserDailyCalorieGoal(userId)
        );

        totalProteinForSelectedDate = Transformations.switchMap(
                selectedDate, date -> Transformations.switchMap(
                        currentUserId, userId -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = calendar.getTime();
                            
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            Date endOfDay = calendar.getTime();
                            
                            return mealEntryRepository.getTotalProteinForDay(userId, startOfDay, endOfDay);
                        }
                )
        );

        totalCarbsForSelectedDate = Transformations.switchMap(
                selectedDate, date -> Transformations.switchMap(
                        currentUserId, userId -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = calendar.getTime();
                            
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            Date endOfDay = calendar.getTime();
                            
                            return mealEntryRepository.getTotalCarbsForDay(userId, startOfDay, endOfDay);
                        }
                )
        );

        totalFatForSelectedDate = Transformations.switchMap(
                selectedDate, date -> Transformations.switchMap(
                        currentUserId, userId -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = calendar.getTime();
                            
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            Date endOfDay = calendar.getTime();
                            
                            return mealEntryRepository.getTotalFatForDay(userId, startOfDay, endOfDay);
                        }
                )
        );
    }

    public void moveToNextDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate.getValue());
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);

        if (!calendar.after(today)) {
            selectedDate.setValue(calendar.getTime());
        }
    }

    public void moveToPreviousDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate.getValue());
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        selectedDate.setValue(calendar.getTime());
    }

    public void moveToToday() {
        selectedDate.setValue(new Date());
    }

    public void setSelectedDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        Date normalizedDate = calendar.getTime();
        System.out.println("DEBUG: DiaryViewModel setting selected date to: " + normalizedDate);

        selectedDate.setValue(normalizedDate);
        System.out.println("DEBUG: DiaryViewModel date updated, refreshing data");

        refreshData();
    }

    private Calendar getCalendarForDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public LiveData<String> getFormattedDate() {
        return Transformations.map(selectedDate, date -> {
            Calendar today = Calendar.getInstance();
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(date);
            
            if (isSameDay(today, selectedCal)) {
                return "Today, " + dateFormat.format(date);
            } else {
                return dateFormat.format(date);
            }
        });
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public LiveData<Map<String, List<MealEntry>>> getMealEntriesGroupedByType() {
        return Transformations.map(mealEntriesForSelectedDate, entries -> {
            if (entries == null) {
                return Map.of();
            }
            return entries.stream()
                    .collect(Collectors.groupingBy(MealEntry::getMealType));
        });
    }

    public LiveData<Integer> getTotalCaloriesForSelectedDate() {
        return totalCaloriesForSelectedDate;
    }

    public LiveData<Float> getTotalProteinForSelectedDate() {
        return totalProteinForSelectedDate;
    }

    public LiveData<Float> getTotalCarbsForSelectedDate() {
        return totalCarbsForSelectedDate;
    }

    public LiveData<Float> getTotalFatForSelectedDate() {
        return totalFatForSelectedDate;
    }

    public LiveData<Integer> getRemainingCalories() {
        return Transformations.switchMap(dailyCalorieGoal, goal -> 
                Transformations.map(totalCaloriesForSelectedDate, consumed -> {
                    if (goal == null) {
                        return 0;
                    }
                    if (consumed == null) {
                        return goal;  // Return full goal when no calories consumed
                    }
                    return goal - consumed;
                })
        );
    }

    public LiveData<Integer> getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void refreshUserData() {
        currentUserId.setValue(currentUserId.getValue());
    }

    public LiveData<Food> getFoodById(long foodId) {
        return foodRepository.getFoodById(foodId);
    }

    public void addMealEntry(MealEntry mealEntry) {
        System.out.println("DEBUG: Adding meal entry - Food ID: " + mealEntry.getFoodId() + 
                          ", Meal type: " + mealEntry.getMealType() + 
                          ", Date: " + mealEntry.getDate());

        Date entryDate = mealEntry.getDate();
        if (entryDate == null) {
            System.out.println("ERROR: Meal entry has null date");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(entryDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        entryDate = calendar.getTime();

        mealEntry.setDate(entryDate);
        System.out.println("DEBUG: Using normalized date from meal entry: " + entryDate);

        Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(entryDate);
        queryCal.set(Calendar.HOUR_OF_DAY, 0);
        queryCal.set(Calendar.MINUTE, 0);
        queryCal.set(Calendar.SECOND, 0);
        queryCal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = queryCal.getTime();
        
        queryCal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = queryCal.getTime();

        System.out.println("DEBUG: FINAL DATE CHECK - Adding meal to date: " + mealEntry.getDate());

        mealEntryRepository.insertOrUpdateMealEntry(mealEntry, startOfDay, endOfDay);

        refreshData();
    }

    public void refreshData() {
        Date currentDate = selectedDate.getValue();
        if (currentDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            Date normalizedDate = calendar.getTime();

            if (!normalizedDate.equals(selectedDate.getValue())) {
                selectedDate.setValue(normalizedDate);
                System.out.println("DEBUG: Refreshing diary data with new normalized date: " + normalizedDate);
            }
        }
    }

    public void updateMealEntry(MealEntry mealEntry) {
        System.out.println("DEBUG: DiaryViewModel updating meal entry with ID: " + mealEntry.getId());

        if (mealEntry.getId() <= 0) {
            System.out.println("DEBUG: ERROR - Cannot update meal entry with invalid ID 0, will not proceed");
            return;
        }

        System.out.println("DEBUG: Performing direct update on meal entry: " + 
                          "ID=" + mealEntry.getId() + 
                          ", Food=" + mealEntry.getFoodId() + 
                          ", MealType=" + mealEntry.getMealType() + 
                          ", Servings=" + mealEntry.getServingsConsumed());

        mealEntryRepository.update(mealEntry);
    }

    public void deleteMealEntry(MealEntry mealEntry) {
        mealEntryRepository.delete(mealEntry);
    }

    public LiveData<List<Food>> getAllFoods() {
        return foodRepository.getAllFoodEntities();
    }

    public LiveData<List<Food>> searchFoods(String query) {
        return foodRepository.searchFoodsByName(query);
    }

    public MutableLiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        
        public Factory(Application application) {
            this.application = application;
        }
        
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(DiaryViewModel.class)) {
                return (T) new DiaryViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
} 