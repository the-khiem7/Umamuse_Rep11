package com.example.umamuse;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.umamuse.services.BackgroundMusicService;

/**
 * Application class to track app lifecycle and handle music when app goes to background
 */
public class UmamuseApplication extends Application {
    private static final String TAG = "UmamuseApplication";
    
    private int activityReferences = 0;
    private boolean isAppInForeground = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }
    
    private class AppLifecycleTracker implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            // Not needed
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (++activityReferences == 1 && !isAppInForeground) {
                // App enters foreground
                isAppInForeground = true;
                Log.d(TAG, "App goes to foreground");
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            // Not needed
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            // Not needed
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (--activityReferences == 0) {
                // App enters background
                isAppInForeground = false;
                Log.d(TAG, "App goes to background");
                
                // Stop the music service when app goes to background
                Intent intent = new Intent(activity, BackgroundMusicService.class);
                intent.setAction("ACTION_STOP_SERVICE");
                activity.startService(intent);
                Log.d(TAG, "Music service stop requested");
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            // Not needed
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            // Not needed
        }
    }
}