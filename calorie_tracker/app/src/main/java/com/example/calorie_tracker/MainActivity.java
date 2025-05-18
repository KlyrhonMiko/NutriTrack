package com.example.calorie_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.calorie_tracker.ui.diary.DiaryFragment;
import com.example.calorie_tracker.ui.food.FoodFragment;
import com.example.calorie_tracker.ui.profile.ProfileFragment;
import com.example.calorie_tracker.ui.progress.ProgressFragment;
import com.example.calorie_tracker.utils.ThemeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment diaryFragment, foodFragment, progressFragment, profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force light mode before setting content view
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ThemeUtils.forceLightMode();
        
        super.onCreate(savedInstanceState);
        
        // Apply light mode to this activity specifically
        ThemeUtils.forceLightModeForActivity(this);
        
        // Check if user profile is set up
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isProfileSetup = sharedPreferences.getBoolean("isProfileSetup", false);
        
        if (!isProfileSetup) {
            // If profile is not set up, redirect to onboarding
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Continue with normal setup if profile exists
        setContentView(R.layout.activity_main);

        // Initialize fragments
        diaryFragment = new DiaryFragment();
        foodFragment = new FoodFragment();
        progressFragment = new ProgressFragment();
        profileFragment = new ProfileFragment();
        fragmentManager = getSupportFragmentManager();
        
        // Load initial fragment
        loadFragment(diaryFragment);

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_diary) {
                loadFragment(diaryFragment);
                return true;
            } else if (itemId == R.id.navigation_foods) {
                loadFragment(foodFragment);
                return true;
            } else if (itemId == R.id.navigation_progress) {
                loadFragment(progressFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                loadFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply light mode when the activity resumes
        ThemeUtils.forceLightModeForActivity(this);
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    /**
     * Navigate to the FoodFragment with optional meal type
     * @param mealType Optional meal type parameter
     */
    public void navigateToFoodFragment(String mealType) {
        // Create new instance of FoodFragment
        FoodFragment newFoodFragment = new FoodFragment();
        
        // If meal type is provided, pass it as an argument
        if (mealType != null && !mealType.isEmpty()) {
            Bundle args = new Bundle();
            args.putString("mealType", mealType);
            newFoodFragment.setArguments(args);
        }
        
        // Load the fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, newFoodFragment);
        transaction.addToBackStack(null);  // Allow back navigation
        transaction.commit();
    }

    /**
     * Navigate directly to the DiaryFragment
     */
    public void loadDiaryFragment() {
        // Create a new instance of DiaryFragment instead of reusing the cached one
        // This ensures the fragment will respect the selected date in the ViewModel
        DiaryFragment newDiaryFragment = new DiaryFragment();
        
        // Load the new fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, newDiaryFragment);
        transaction.commit();
        
        // Update the cached instance
        diaryFragment = newDiaryFragment;
        
        // Update bottom navigation if present
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_diary);
        }
    }
}