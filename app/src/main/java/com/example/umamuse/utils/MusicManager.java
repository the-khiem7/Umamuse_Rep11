package com.example.umamuse.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.ToggleButton;

import com.example.umamuse.services.BackgroundMusicService;

/**
 * Helper class to manage music service connection and control across activities
 */
public class MusicManager {

    private BackgroundMusicService musicService;
    private boolean isBound = false;
    private final Activity activity;
    private final ToggleButton soundToggleButton;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundMusicService.MusicBinder binder = (BackgroundMusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            
            // Set the toggle button's state based on the music service's mute state
            if (soundToggleButton != null) {
                soundToggleButton.setChecked(musicService.isMuted());
            }
            
            // Start music if not muted
            musicService.startMusic();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
        }
    };

    public MusicManager(Activity activity, ToggleButton soundToggleButton) {
        this.activity = activity;
        this.soundToggleButton = soundToggleButton;
        
        // Set toggle button click listener
        if (soundToggleButton != null) {
            soundToggleButton.setOnClickListener(v -> {
                if (isBound && musicService != null) {
                    boolean isMuted = musicService.toggleMute();
                    soundToggleButton.setChecked(isMuted);
                }
            });
        }
    }

    public void bindMusicService() {
        Intent intent = new Intent(activity, BackgroundMusicService.class);
        activity.startService(intent); // Start the service if it's not already started
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindMusicService() {
        if (isBound) {
            activity.unbindService(serviceConnection);
            isBound = false;
        }
    }

    public boolean isMusicMuted() {
        return musicService != null && musicService.isMuted();
    }

    public void toggleMute() {
        if (isBound && musicService != null) {
            boolean isMuted = musicService.toggleMute();
            if (soundToggleButton != null) {
                soundToggleButton.setChecked(isMuted);
            }
        }
    }
    
    /**
     * Chuyển sang bài hát tiếp theo
     */
    public void nextTrack() {
        if (isBound && musicService != null) {
            musicService.changeToNextTrack();
        }
    }
    
    /**
     * Chuyển sang bài hát trước đó
     */
    public void previousTrack() {
        if (isBound && musicService != null) {
            musicService.changeToPreviousTrack();
        }
    }
    
    /**
     * Lấy tên bài hát đang phát hiện tại
     * @return Tên bài hát hiện tại hoặc null nếu không có bài hát nào đang phát
     */
    public String getCurrentTrackName() {
        if (isBound && musicService != null) {
            return musicService.getCurrentTrackName();
        }
        return null;
    }
    
    /**
     * Gọi phương thức này khi thoát ứng dụng để dừng service
     */
    public void stopMusicService() {
        // Unbind first to avoid service auto-restart
        if (isBound) {
            try {
                activity.unbindService(serviceConnection);
                isBound = false;
            } catch (Exception e) {
                // Ignore if already unbound
            }
        }
        
        // Then send stop command
        Intent intent = new Intent(activity, BackgroundMusicService.class);
        intent.setAction("ACTION_STOP_SERVICE");
        activity.startService(intent);
    }
}