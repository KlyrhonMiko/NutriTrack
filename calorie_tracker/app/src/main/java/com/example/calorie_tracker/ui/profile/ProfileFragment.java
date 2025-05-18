package com.example.calorie_tracker.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.ui.profile.EditProfileDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    
    private TextView userNameTextView;
    private TextView userStatsTextView;
    private TextView weightValueTextView;
    private TextView heightValueTextView;
    private TextView goalValueTextView;
    private TextView activityLevelTextView;
    private TextView bmiTextView;
    private TextView calorieGoalTextView;
    private TextView weightGoalTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        
        initViews(root);
        setupListeners(root);
        observeViewModel();
        
        return root;
    }
    
    private void initViews(View root) {
        userNameTextView = root.findViewById(R.id.userNameTextView);
        userStatsTextView = root.findViewById(R.id.userStatsTextView);
        weightValueTextView = root.findViewById(R.id.weightValueTextView);
        heightValueTextView = root.findViewById(R.id.heightValueTextView);
        goalValueTextView = root.findViewById(R.id.goalValueTextView);
        activityLevelTextView = root.findViewById(R.id.activityLevelTextView);
        bmiTextView = root.findViewById(R.id.bmiTextView);
        calorieGoalTextView = root.findViewById(R.id.calorieGoalTextView);
        weightGoalTextView = root.findViewById(R.id.weightGoalTextView);

        loadUserData();
    }
    
    private void loadUserData() {
        String name = viewModel.getUserName();
        int age = viewModel.getUserAge();
        String gender = viewModel.getUserGender();
        float height = viewModel.getUserHeight();
        float weight = viewModel.getUserWeight();
        int activityLevel = viewModel.getUserActivityLevel();
        String weightGoalType = viewModel.getUserWeightGoalType();
        
        // Calculate BMI
        String bmiText = viewModel.calculateBMI(weight, height);

        int dailyCalorieGoal = viewModel.calculateDailyCalorieGoal(gender, weight, height, age, activityLevel, weightGoalType);

        String activityLevelStr;
        switch (activityLevel) {
            case 1:
                activityLevelStr = getString(R.string.activity_sedentary_short);
                break;
            case 2:
                activityLevelStr = getString(R.string.activity_light_short);
                break;
            case 3:
                activityLevelStr = getString(R.string.activity_moderate_short);
                break;
            case 4:
                activityLevelStr = getString(R.string.activity_very_short);
                break;
            case 5:
                activityLevelStr = getString(R.string.activity_extra_short);
                break;
            default:
                activityLevelStr = getString(R.string.activity_moderate_short);
        }

        userNameTextView.setText(name);

        String formattedGender = (gender != null && gender.length() > 0) ? 
            gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase() : "";
        if (age > 0 && !formattedGender.isEmpty()) {
            userStatsTextView.setText(age + " years, " + formattedGender);
        } else if (age > 0) {
            userStatsTextView.setText(age + " years");
        } else if (!formattedGender.isEmpty()) {
            userStatsTextView.setText(formattedGender);
        } else {
            userStatsTextView.setText("");
        }
        
        weightValueTextView.setText(weight + " kg");
        heightValueTextView.setText(height + " cm");

        goalValueTextView.setText(dailyCalorieGoal + " kcal");
        
        activityLevelTextView.setText(activityLevelStr);
        bmiTextView.setText(bmiText);
        calorieGoalTextView.setText(dailyCalorieGoal + " cal");

        weightGoalTextView.setText(weightGoalType);
    }
    
    private void setupListeners(View root) {
        MaterialButton editProfileButton = root.findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(v -> {
            editProfileButton.setEnabled(false);

            editProfileButton.setAlpha(0.7f);

            EditProfileDialogFragment dialogFragment = EditProfileDialogFragment.newInstance();

            dialogFragment.setOnDismissListener(() -> {
                editProfileButton.setEnabled(true);
                editProfileButton.setAlpha(1.0f);
            });
            
            dialogFragment.show(getParentFragmentManager(), "edit_profile");
        });
    }
    
    private void observeViewModel() {
        viewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                loadUserData();
            }
        });

        viewModel.getPreferencesUpdateEvent().observe(getViewLifecycleOwner(), isUpdated -> {
            if (isUpdated) {
                loadUserData();
            }
        });
    }
} 