package com.example.collegeelectionsystem;

import android.app.Application;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Subscribe globally for every app install
        FirebaseMessaging.getInstance().subscribeToTopic("announcements");
        FirebaseMessaging.getInstance().subscribeToTopic("results");
    }
}
