package com.example.calorie_tracker.data.model;

import android.util.Log;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private int age;
    private String gender;
    private float height; // in cm
    private float weight; // in kg
    private int activityLevel; // 1-5 (sedentary to very active)
    private String weightGoalType; // "Lose", "Maintain", "Gain"
    private int dailyCalorieGoal;

    public User(String name, int age, String gender, float height, float weight, int activityLevel, String weightGoalType) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.activityLevel = activityLevel;
        this.weightGoalType = weightGoalType;
        calculateCalorieGoal();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(int activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public String getWeightGoalType() {
        return weightGoalType;
    }
    
    public void setWeightGoalType(String weightGoalType) {
        this.weightGoalType = weightGoalType;
    }

    public int getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public void recalculateCalorieGoal() {
        calculateCalorieGoal();
    }

    // Calculate calorie goal based on user's biometrics and weight goal
    private void calculateCalorieGoal() {
        System.out.println("DEBUG: Calculating calorie goal");
        System.out.println("DEBUG: Input values - Gender: " + gender + ", Weight: " + weight + ", Height: " + height + ", Age: " + age);
        
        // Harris-Benedict equation for BMR (Basal Metabolic Rate)
        float bmr;
        if (gender != null && (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("Male"))) {
            bmr = 88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * age);
            System.out.println("DEBUG: Using male BMR formula");
        } else {
            bmr = 447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * age);
            System.out.println("DEBUG: Using female BMR formula");
        }
        
        System.out.println("DEBUG: Calculated BMR: " + bmr);

        // Multiply BMR by activity factor
        float activityFactor;
        switch (activityLevel) {
            case 1: // Sedentary
                activityFactor = 1.2f;
                break;
            case 2: // Lightly active
                activityFactor = 1.375f;
                break;
            case 3: // Moderately active
                activityFactor = 1.55f;
                break;
            case 4: // Very active
                activityFactor = 1.725f;
                break;
            case 5: // Extra active
                activityFactor = 1.9f;
                break;
            default:
                activityFactor = 1.2f;
        }
        
        System.out.println("DEBUG: Activity factor: " + activityFactor);

        // Calculate TDEE (Total Daily Energy Expenditure)
        float tdee = bmr * activityFactor;
        System.out.println("DEBUG: TDEE before goal adjustment: " + tdee);
        
        // Adjust calories based on weight goal type
        if (weightGoalType != null) {
            if (weightGoalType.equalsIgnoreCase("Lose") || weightGoalType.equalsIgnoreCase("lose weight")) {
                // 20% calorie deficit for weight loss (approximately 1 pound per week)
                tdee *= 0.8f;
                System.out.println("DEBUG: Applied 20% deficit for weight loss");
            } else if (weightGoalType.equalsIgnoreCase("Gain") || weightGoalType.equalsIgnoreCase("gain weight")) {
                // 15% calorie surplus for weight gain
                tdee *= 1.15f;
                System.out.println("DEBUG: Applied 15% surplus for weight gain");
            }
            // For "Maintain", keep TDEE as is
        }

        dailyCalorieGoal = Math.round(tdee);
        System.out.println("DEBUG: Final daily calorie goal: " + dailyCalorieGoal);
    }
} 