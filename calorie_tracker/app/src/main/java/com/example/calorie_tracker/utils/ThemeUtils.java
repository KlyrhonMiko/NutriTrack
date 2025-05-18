package com.example.calorie_tracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class to enforce light theme across the app regardless of system settings
 */
public class ThemeUtils {

    public static void forceLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void forceLightModeForActivity(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activity.getWindow().getDecorView().setForceDarkAllowed(false);
            }

            Configuration configuration = activity.getResources().getConfiguration();
            configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) 
                    | Configuration.UI_MODE_NIGHT_NO;
            activity.getResources().updateConfiguration(configuration, 
                    activity.getResources().getDisplayMetrics());
        }
    }
} 