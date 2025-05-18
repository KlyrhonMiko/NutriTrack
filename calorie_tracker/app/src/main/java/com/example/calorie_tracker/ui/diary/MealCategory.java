package com.example.calorie_tracker.ui.diary;

import com.example.calorie_tracker.data.model.MealEntry;

import java.util.List;

/**
 * Model class representing a meal category with its associated entries
 */
public class MealCategory {
    private String mealType;
    private int totalCalories;
    private List<MealEntry> entries;

    public MealCategory(String mealType, int totalCalories, List<MealEntry> entries) {
        this.mealType = mealType;
        this.totalCalories = totalCalories;
        this.entries = entries;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public List<MealEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MealEntry> entries) {
        this.entries = entries;
    }

    public String getDisplayName() {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return "Breakfast";
            case "lunch":
                return "Lunch";
            case "dinner":
                return "Dinner";
            case "snack":
                return "Snack";
            default:
                return mealType;
        }
    }

    public String getTimeRange() {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return "6:00 AM - 10:00 AM";
            case "lunch":
                return "11:00 AM - 3:00 PM";
            case "dinner":
                return "5:00 PM - 9:00 PM";
            case "snack":
                return "Any time";
            default:
                return "";
        }
    }

    public String getColorResourceName() {
        return mealType.toLowerCase();
    }
} 