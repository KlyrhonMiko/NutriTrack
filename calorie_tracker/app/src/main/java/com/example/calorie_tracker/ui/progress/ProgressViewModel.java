package com.example.calorie_tracker.ui.progress;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.calorie_tracker.data.model.MealEntry;
import com.example.calorie_tracker.data.repository.MealEntryRepository;
import com.example.calorie_tracker.data.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the Progress screen
 */
public class ProgressViewModel extends AndroidViewModel {
    
    private static final String PREFS_NAME = "UserPrefs";
    private static final String WEIGHT_ENTRIES_KEY = "weightEntries";
    
    private TimeRange selectedTimeRange = TimeRange.WEEK;
    private final List<MealEntry> calorieEntries = new ArrayList<>();
    private final List<WeightEntry> weightEntries = new ArrayList<>();
    private float initialWeight = 0f;
    private final Gson gson;
    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<MealEntry>> calorieEntriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> dailyCalorieGoal = new MutableLiveData<>();
    
    public enum TimeRange {
        WEEK, MONTH, ALL
    }
    
    public ProgressViewModel(Application application) {
        super(application);
        gson = new Gson();
        mealEntryRepository = new MealEntryRepository(application);
        userRepository = new UserRepository(application);
        
        loadWeightEntries();
        loadInitialWeight();
        loadCalorieEntries();
        loadCalorieGoal();
    }
    
    private void loadCalorieGoal() {
        System.out.println("DEBUG: Loading calorie goal");
        userRepository.getActiveUser().observeForever(user -> {
            if (user != null) {
                System.out.println("DEBUG: Active user found - ID: " + user.getId() + ", Goal: " + user.getDailyCalorieGoal());
                dailyCalorieGoal.setValue(user.getDailyCalorieGoal());
            } else {
                System.out.println("DEBUG: No active user found, using default goal: 2000");
                dailyCalorieGoal.setValue(2000);
            }
        });
    }
    
    public LiveData<Integer> getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }
    
    private void loadCalorieEntries() {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_YEAR, -getDaysForTimeRange());
        Date startDate = cal.getTime();

        mealEntryRepository.getMealEntriesByDateRange(1L, startDate, endDate)
            .observeForever(entries -> {
                if (entries != null) {
                    calorieEntries.clear();
                    calorieEntries.addAll(entries);
                    calorieEntriesLiveData.postValue(entries);
                } else {
                    calorieEntries.clear();
                    calorieEntriesLiveData.postValue(new ArrayList<>());
                }
            });
    }
    
    private int getDaysForTimeRange() {
        switch (selectedTimeRange) {
            case WEEK:
                return 7;
            case MONTH:
                return 30;
            case ALL:
            default:
                return 365;
        }
    }
    
    public void setTimeRange(TimeRange timeRange) {
        this.selectedTimeRange = timeRange;
        loadCalorieEntries();
    }
    
    public TimeRange getSelectedTimeRange() {
        return selectedTimeRange;
    }
    
    public List<MealEntry> getCalorieEntries() {
        return calorieEntries;
    }
    
    public LiveData<List<MealEntry>> getCalorieEntriesLiveData() {
        return calorieEntriesLiveData;
    }
    
    public List<WeightEntry> getWeightEntries() {
        return filterEntriesByTimeRange(weightEntries);
    }

    public List<WeightEntry> getAllWeightEntries() {
        return weightEntries;
    }

    private <T> List<T> filterEntriesByTimeRange(List<T> entries) {
        if (entries == null || entries.isEmpty() || selectedTimeRange == TimeRange.ALL) {
            return entries;
        }
        
        List<T> filteredEntries = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        Calendar entryDate = Calendar.getInstance();
        
        for (T entry : entries) {
            Date date = null;
            
            if (entry instanceof MealEntry) {
                date = ((MealEntry) entry).getDate();
            } else if (entry instanceof WeightEntry) {
                date = ((WeightEntry) entry).getDate();
            }
            
            if (date != null) {
                entryDate.setTime(date);
                
                if (selectedTimeRange == TimeRange.WEEK) {
                    Calendar weekAgo = Calendar.getInstance();
                    weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                    
                    if (entryDate.after(weekAgo) || isSameDay(entryDate, weekAgo)) {
                        filteredEntries.add(entry);
                    }
                } else if (selectedTimeRange == TimeRange.MONTH) {
                    Calendar monthAgo = Calendar.getInstance();
                    monthAgo.add(Calendar.DAY_OF_YEAR, -30);
                    
                    if (entryDate.after(monthAgo) || isSameDay(entryDate, monthAgo)) {
                        filteredEntries.add(entry);
                    }
                }
            }
        }
        
        return filteredEntries;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && 
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public void addWeightEntry(WeightEntry entry) {
        if (entry != null) {
            boolean entryUpdated = false;
            
            for (int i = 0; i < weightEntries.size(); i++) {
                WeightEntry existingEntry = weightEntries.get(i);
                
                Calendar existingCal = Calendar.getInstance();
                existingCal.setTime(existingEntry.getDate());
                
                Calendar newCal = Calendar.getInstance();
                newCal.setTime(entry.getDate());
                
                if (isSameDay(existingCal, newCal)) {
                    weightEntries.set(i, entry);
                    entryUpdated = true;
                    break;
                }
            }

            if (!entryUpdated) {
                weightEntries.add(entry);
            }

            saveWeightEntries();

            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("weight", entry.getWeight());
            editor.apply();
        }
    }

    private void saveWeightEntries() {
        try {
            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
            String jsonWeightEntries = gson.toJson(weightEntries);
            editor.putString(WEIGHT_ENTRIES_KEY, jsonWeightEntries);
            editor.apply();
        } catch (Exception e) {
        }
    }

    private void loadWeightEntries() {
        try {
            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String jsonWeightEntries = sharedPreferences.getString(WEIGHT_ENTRIES_KEY, null);
            
            if (jsonWeightEntries != null) {
                Type type = new TypeToken<ArrayList<WeightEntry>>() {}.getType();
                List<WeightEntry> savedEntries = gson.fromJson(jsonWeightEntries, type);
                if (savedEntries != null) {
                    weightEntries.clear();
                    weightEntries.addAll(savedEntries);
                }
            }
        } catch (Exception e) {
        }
    }

    private void loadInitialWeight() {
        try {
            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            initialWeight = sharedPreferences.getFloat("weight", 0f);

            if (initialWeight > 0 && weightEntries.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                WeightEntry initialEntry = new WeightEntry(cal.getTime(), initialWeight);
                weightEntries.add(initialEntry);
                saveWeightEntries();
            }
        } catch (Exception e) {
        }
    }

    public float getInitialWeight() {
        return initialWeight;
    }
    
    public void reloadCalorieGoal() {
        System.out.println("DEBUG: Forcing reload of calorie goal");
        loadCalorieGoal();
    }
    
    public static class WeightEntry {
        private final Date date;
        private final float weight;
        
        public WeightEntry(Date date, float weight) {
            this.date = date;
            this.weight = weight;
        }
        
        public Date getDate() {
            return date;
        }
        
        public float getWeight() {
            return weight;
        }
    }
} 