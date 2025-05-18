package com.example.calorie_tracker;

import android.app.Application;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Custom Application class that ensures the app always uses light mode
 * regardless of the device settings
 */
public class CalorieTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Force the app to always use light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // Ensure configuration is set to light mode
        Configuration configuration = getResources().getConfiguration();
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) 
                | Configuration.UI_MODE_NIGHT_NO;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }
} 