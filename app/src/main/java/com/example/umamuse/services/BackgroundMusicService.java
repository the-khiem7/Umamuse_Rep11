package com.example.umamuse.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.umamuse.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Delayed;

public class BackgroundMusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "BackgroundMusicService";
    private static final String PREFS_NAME = "UmamuseMusicPrefs";
    private static final String PREF_IS_MUTED = "is_muted";
    private static final float DEFAULT_VOLUME = 0.2f; // 20% volume

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MusicBinder();
    private List<Integer> trackResourceIds;
    private int currentTrackIndex = 0;
    private boolean isPlaying = false;
    private boolean isMuted = false;

    public class MusicBinder extends Binder {
        public BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize tracks and shuffle them
        trackResourceIds = new ArrayList<>(Arrays.asList(
            R.raw.bakushin,
            R.raw.track1,
            R.raw.track2,
            R.raw.track3
        ));
        Collections.shuffle(trackResourceIds);
        
        // Load mute preference
        loadMutePreference();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void loadMutePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isMuted = prefs.getBoolean(PREF_IS_MUTED, false);
    }

    public void saveMutePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_IS_MUTED, isMuted);
        editor.apply();
    }

    public void startMusic() {
        if (mediaPlayer == null) {
            setupMediaPlayer();
        }

        if (!mediaPlayer.isPlaying() && !isMuted) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void setupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, trackResourceIds.get(currentTrackIndex));
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME);
        mediaPlayer.setLooping(false); // Don't loop, we'll handle track transitions manually
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // When a track finishes, move to the next one
        nextTrack();
    }

    private void nextTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Update the current track index before the delay
        currentTrackIndex = (currentTrackIndex + 1) % trackResourceIds.size();
        
        new android.os.Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                setupMediaPlayer();
                startMusic(); // Start the next track if not muted
            }
        }, 5000);
    }

    public boolean toggleMute() {
        isMuted = !isMuted;
        
        if (isMuted) {
            pauseMusic();
        } else {
            startMusic();
        }
        
        saveMutePreference();
        return isMuted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if ("ACTION_STOP_SERVICE".equals(action)) {
                // Dừng service khi ứng dụng thoát hoặc đi vào background
                Log.d(TAG, "Stopping music service by explicit request");
                
                // Ensure media player is stopped and released
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                
                // Stop the service
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        // Nếu service bị kill, khởi động lại
        return START_STICKY;
    }
    
    public void shuffleTracks() {
        Collections.shuffle(trackResourceIds);
        currentTrackIndex = 0;
    }
    
    /**
     * Chuyển sang bài hát tiếp theo trong danh sách
     */
    public void changeToNextTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        currentTrackIndex = (currentTrackIndex + 1) % trackResourceIds.size();
        setupMediaPlayer();
        startMusic();
    }
    
    /**
     * Chuyển sang bài hát trước đó trong danh sách
     */
    public void changeToPreviousTrack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        currentTrackIndex = (currentTrackIndex - 1 + trackResourceIds.size()) % trackResourceIds.size();
        setupMediaPlayer();
        startMusic();
    }
    
    /**
     * Lấy tên bài hát hiện tại (không bao gồm phần mở rộng)
     */
    public String getCurrentTrackName() {
        int resourceId = trackResourceIds.get(currentTrackIndex);
        String resourceEntryName = getResources().getResourceEntryName(resourceId);
        return resourceEntryName;
    }
}