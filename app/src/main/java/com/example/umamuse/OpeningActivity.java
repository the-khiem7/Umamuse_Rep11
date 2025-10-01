package com.example.umamuse;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.graphics.Point;
import android.util.DisplayMetrics;
import java.util.Random;

import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class OpeningActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btnSkip;
    private boolean isVideoCompleted = false;
    private Handler handler = new Handler();
    private boolean isNavigating = false; // Flag to prevent multiple navigation attempts
    private Random random = new Random(); // For generating random button positions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force landscape orientation for the video
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Make the activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_opening);

        videoView = findViewById(R.id.videoView);
        btnSkip = findViewById(R.id.btnSkip);

        // Set up video path - using the video from raw folder
        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.opening);
            videoView.setVideoURI(videoUri);
            
            // Create media controller with playback controls (optional)
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            
            // Hide the media controller initially
            mediaController.hide();

            videoView.setKeepScreenOn(true);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Start playing once prepared
                    videoView.start();

                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    setVideoFullScreen();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback in case the video can't be loaded
            Toast.makeText(this, "Error loading opening video", Toast.LENGTH_SHORT).show();
        }
        
        // Start playing the video automatically
        videoView.start();

        // Handle video completion
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isVideoCompleted = true;
                
                // Hide the troll button once video is complete
                btnSkip.setVisibility(View.INVISIBLE);
                
                // Show a message that we're continuing to the game
                Toast.makeText(OpeningActivity.this, "36!", Toast.LENGTH_SHORT).show();
                
                // Automatically go to RaceActivity after a 3-second delay when video finishes
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            navigateToRaceActivity();
                        }
                    }
                }, 3000); // 3 seconds delay after video completely finishes
            }
        });

        // Troll Skip button handler - button jumps to a random position when clicked
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instead of navigating, move the button to a random position
                moveButtonToRandomPosition();
            }
        });
        
        // Make the skip button initially visible
        btnSkip.setVisibility(View.VISIBLE);
    }

    private void navigateToRaceActivity() {
        // Prevent multiple navigation attempts
        if (isNavigating) {
            return;
        }
        isNavigating = true;
        
        // Cancel any pending navigation requests
        handler.removeCallbacksAndMessages(null);
        
        Intent intent = new Intent(OpeningActivity.this, RaceActivity.class);
        startActivity(intent);
        finish(); // Close this activity so user can't go back to it
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !isVideoCompleted) {
            videoView.start();
        }
    }

    /**
     * Set the video to full screen mode, ensuring it fills the entire screen
     * with no black bars by using a custom VideoView approach
     */
    private void setVideoFullScreen() {
        // Get the screen dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // Set the VideoView to fill the entire screen
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.width = screenWidth;
        params.height = screenHeight;
        videoView.setLayoutParams(params);
        
        // Set additional properties on the video view container to ensure it fills the screen
        FrameLayout container = findViewById(R.id.videoContainer);
        ViewGroup.LayoutParams containerParams = container.getLayoutParams();
        containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        containerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        container.setLayoutParams(containerParams);
    }
    
    /**
     * Moves the skip button to a random position on the screen with a smooth animation
     * This creates a "troll" effect where the button runs away from the user
     */
    private void moveButtonToRandomPosition() {
        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // Get button dimensions
        int buttonWidth = btnSkip.getWidth();
        int buttonHeight = btnSkip.getHeight();
        
        // Calculate max X and Y positions to keep button fully on screen
        int maxX = screenWidth - buttonWidth - 20;  // leave some margin
        int maxY = screenHeight - buttonHeight - 20; // leave some margin
        
        // Generate random coordinates
        int newX = random.nextInt(maxX);
        int newY = random.nextInt(maxY);
        
        // Create X and Y animations
        ObjectAnimator animX = ObjectAnimator.ofFloat(btnSkip, "x", newX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(btnSkip, "y", newY);
        
        // Set animation properties - make it quick and bouncy
        animX.setDuration(300);
        animY.setDuration(300);
        animX.setInterpolator(new AccelerateDecelerateInterpolator());
        animY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Start animations
        animX.start();
        animY.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all pending posts of callbacks and sent messages
        handler.removeCallbacksAndMessages(null);
        
        // Release media player resources if available
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }
}