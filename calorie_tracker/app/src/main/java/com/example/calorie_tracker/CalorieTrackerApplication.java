package com.example.calorie_tracker;

import android.app.Application;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class CalorieTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Configuration configuration = getResources().getConfiguration();
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) 
                | Configuration.UI_MODE_NIGHT_NO;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }
} 