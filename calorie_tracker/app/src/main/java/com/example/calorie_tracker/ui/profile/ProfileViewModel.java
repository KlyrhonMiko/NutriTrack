package com.example.calorie_tracker.ui.profile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.calorie_tracker.data.model.User;
import com.example.calorie_tracker.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> userData;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<Boolean> preferencesUpdateEvent = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        userData = userRepository.getActiveUser();
        sharedPreferences = application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        checkAndCreateUserFromPrefs();
    }

    private void checkAndCreateUserFromPrefs() {
        boolean isProfileSetup = sharedPreferences.getBoolean("isProfileSetup", false);
        
        System.out.println("DEBUG: Checking if profile is set up: " + isProfileSetup);
        
        if (isProfileSetup) {
            String name = sharedPreferences.getString("name", "");
            int age = sharedPreferences.getInt("age", 0);
            String gender = sharedPreferences.getString("gender", "");
            float height = sharedPreferences.getFloat("height", 0f);
            float weight = sharedPreferences.getFloat("weight", 0f);
            int activityLevel = sharedPreferences.getInt("activityLevel", 1);
            String weightGoalType = sharedPreferences.getString("weightGoalType", "Maintain");
            
            System.out.println("DEBUG: Found user data in SharedPreferences:");
            System.out.println("DEBUG: Name: " + name);
            System.out.println("DEBUG: Age: " + age);
            System.out.println("DEBUG: Gender: " + gender);
            System.out.println("DEBUG: Height: " + height);
            System.out.println("DEBUG: Weight: " + weight);
            System.out.println("DEBUG: Activity Level: " + activityLevel);
            System.out.println("DEBUG: Weight Goal Type: " + weightGoalType);

            User newUser = new User(name, age, gender, height, weight, activityLevel, weightGoalType);
            System.out.println("DEBUG: Created new user with calorie goal: " + newUser.getDailyCalorieGoal());

            userRepository.insert(newUser);
            System.out.println("DEBUG: User inserted into database");
        } else {
            System.out.println("DEBUG: No user data found in SharedPreferences");
        }
    }

    public LiveData<User> getUserData() {
        return userData;
    }

    public String getUserName() {
        return sharedPreferences.getString("name", "");
    }
    
    public int getUserAge() {
        return sharedPreferences.getInt("age", 0);
    }
    
    public String getUserGender() {
        return sharedPreferences.getString("gender", "");
    }
    
    public float getUserHeight() {
        return sharedPreferences.getFloat("height", 0f);
    }
    
    public float getUserWeight() {
        try {
            com.example.calorie_tracker.ui.progress.ProgressViewModel progressViewModel = 
                new com.example.calorie_tracker.ui.progress.ProgressViewModel(getApplication());
            
            List<com.example.calorie_tracker.ui.progress.ProgressViewModel.WeightEntry> weightEntries = 
                progressViewModel.getAllWeightEntries();
            
            if (weightEntries != null && !weightEntries.isEmpty()) {
                List<com.example.calorie_tracker.ui.progress.ProgressViewModel.WeightEntry> sortedEntries = 
                    new ArrayList<>(weightEntries);
                sortedEntries.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));

                return sortedEntries.get(0).getWeight();
            }
        } catch (Exception e) {
        }

        return sharedPreferences.getFloat("weight", 0f);
    }
    
    public int getUserActivityLevel() {
        return sharedPreferences.getInt("activityLevel", 1);
    }
    
    public String getUserWeightGoalType() {
        return sharedPreferences.getString("weightGoalType", "Maintain");
    }

    public String calculateBMI(float weightKg, float heightCm) {
        if (heightCm <= 0 || weightKg <= 0) {
            return "N/A";
        }

        float heightM = heightCm / 100;
        float bmi = weightKg / (heightM * heightM);
        
        String category;
        if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi < 25) {
            category = "Normal";
        } else if (bmi < 30) {
            category = "Overweight";
        } else {
            category = "Obese";
        }
        
        return String.format("%.1f (%s)", bmi, category);
    }

    public int calculateDailyCalorieGoal(String gender, float weight, float height, int age, int activityLevel, String weightGoalType) {
        double bmr;
        if (gender.equalsIgnoreCase("Male")) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }

        double activityMultiplier;
        switch (activityLevel) {
            case 1: activityMultiplier = 1.2; break;   // Sedentary
            case 2: activityMultiplier = 1.375; break; // Lightly active
            case 3: activityMultiplier = 1.55; break;  // Moderately active
            case 4: activityMultiplier = 1.725; break; // Very active
            case 5: activityMultiplier = 1.9; break;   // Extra active
            default: activityMultiplier = 1.55;        // Default
        }
        
        double tdee = bmr * activityMultiplier;

        if (weightGoalType != null) {
            if (weightGoalType.equals("Lose")) {
                // 20% calorie deficit for weight loss
                tdee *= 0.8;
            } else if (weightGoalType.equals("Gain")) {
                // 15% calorie surplus for weight gain
                tdee *= 1.15;
            }
            // For "Maintain", keep TDEE as is
        }

        return Math.round((float)tdee);
    }

    public LiveData<Boolean> getPreferencesUpdateEvent() {
        return preferencesUpdateEvent;
    }

    public void updateUserProfile(User updatedUser) {
        updatedUser.recalculateCalorieGoal();

        userRepository.update(updatedUser);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", updatedUser.getName());
        editor.putInt("age", updatedUser.getAge());
        editor.putString("gender", updatedUser.getGender());
        editor.putFloat("height", updatedUser.getHeight());
        editor.putFloat("weight", updatedUser.getWeight());
        editor.putInt("activityLevel", updatedUser.getActivityLevel());
        editor.putString("weightGoalType", updatedUser.getWeightGoalType());
        editor.apply();

        preferencesUpdateEvent.setValue(true);
    }

    public void notifyWeightUpdated() {
        preferencesUpdateEvent.setValue(true);
    }
} 