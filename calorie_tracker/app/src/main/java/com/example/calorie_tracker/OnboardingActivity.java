package com.example.calorie_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.calorie_tracker.data.model.User;
import com.example.calorie_tracker.data.repository.UserRepository;
import com.example.calorie_tracker.util.CustomToast;
import com.example.calorie_tracker.utils.ThemeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OnboardingActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, ageEditText, heightEditText, weightEditText;
    private RadioGroup genderRadioGroup, goalTypeRadioGroup;
    private AutoCompleteTextView activityLevelDropdown;
    private MaterialButton saveProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ThemeUtils.forceLightMode();
        
        super.onCreate(savedInstanceState);

        ThemeUtils.forceLightModeForActivity(this);
        
        setContentView(R.layout.activity_onboarding);

        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        goalTypeRadioGroup = findViewById(R.id.goalTypeRadioGroup);
        activityLevelDropdown = findViewById(R.id.activityLevelDropdown);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, activityLevels);
        activityLevelDropdown.setAdapter(adapter);

        if (activityLevels.length > 0) {
            activityLevelDropdown.setText(activityLevels[0], false);
        }

        if (goalTypeRadioGroup.getChildCount() > 0) {
            RadioButton maintainButton = findViewById(R.id.radioMaintainWeight);
            if (maintainButton != null) {
                maintainButton.setChecked(true);
            }
        }

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveUserData();
                    Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeUtils.forceLightModeForActivity(this);
    }

    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String ageString = ageEditText.getText().toString().trim();
        String heightString = heightEditText.getText().toString().trim();
        String weightString = weightEditText.getText().toString().trim();
        String activityLevel = activityLevelDropdown.getText().toString().trim();

        if (name.isEmpty() || ageString.isEmpty() || heightString.isEmpty() || weightString.isEmpty()) {
            CustomToast.showError(this, "Please fill in all fields");
            return false;
        }

        if (activityLevel.equals("Select your activity level")) {
            CustomToast.showWarning(this, "Please select your activity level");
            return false;
        }

        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            CustomToast.showWarning(this, "Please select your gender");
            return false;
        }

        if (goalTypeRadioGroup.getCheckedRadioButtonId() == -1) {
            CustomToast.showWarning(this, "Please select your weight goal");
            return false;
        }

        return true;
    }

    private void saveUserData() {
        String name = nameEditText.getText().toString().trim();
        int age = Integer.parseInt(ageEditText.getText().toString().trim());
        float height = Float.parseFloat(heightEditText.getText().toString().trim());
        float weight = Float.parseFloat(weightEditText.getText().toString().trim());

        RadioButton selectedGenderButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
        String gender = selectedGenderButton.getText().toString();

        RadioButton selectedGoalButton = findViewById(goalTypeRadioGroup.getCheckedRadioButtonId());
        String weightGoalType = selectedGoalButton.getText().toString();

        String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
        String selectedActivity = activityLevelDropdown.getText().toString();
        int activityLevel = 1;
        
        for (int i = 0; i < activityLevels.length; i++) {
            if (activityLevels[i].equals(selectedActivity)) {
                activityLevel = i + 1;
                break;
            }
        }

        System.out.println("DEBUG: Saving user data:");
        System.out.println("DEBUG: Name: " + name);
        System.out.println("DEBUG: Age: " + age);
        System.out.println("DEBUG: Gender: " + gender);
        System.out.println("DEBUG: Height: " + height);
        System.out.println("DEBUG: Weight: " + weight);
        System.out.println("DEBUG: Activity Level: " + activityLevel);
        System.out.println("DEBUG: Weight Goal Type: " + weightGoalType);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putInt("age", age);
        editor.putString("gender", gender);
        editor.putFloat("height", height);
        editor.putFloat("weight", weight);
        editor.putInt("activityLevel", activityLevel);
        editor.putString("weightGoalType", weightGoalType);
        editor.putBoolean("isProfileSetup", true);
        editor.apply();

        User newUser = new User(name, age, gender, height, weight, activityLevel, weightGoalType);
        System.out.println("DEBUG: Created new user with calorie goal: " + newUser.getDailyCalorieGoal());
        
        UserRepository userRepository = new UserRepository(getApplication());
        userRepository.insert(newUser);
        System.out.println("DEBUG: User saved to database");
    }
} 