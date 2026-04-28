package com.example.lofo;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class LoFoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Force Light Mode (Disable Dark Mode completely)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
