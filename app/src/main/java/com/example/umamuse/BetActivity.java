package com.example.umamuse;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.umamuse.adapters.HorseBetAdapter;
import com.example.umamuse.models.Horse;
import com.example.umamuse.models.HorseBet;
import com.example.umamuse.repositories.HorseRepository;
import com.example.umamuse.utils.MusicManager;
import com.example.umamuse.utils.UserPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BetActivity extends AppCompatActivity {

    private ViewPager2 vpHorses;
    private Button btnConfirmBet, btnBackToRace;
    private TextView tvUserBalance;
    private ToggleButton btnToggleSound;
    private Button btnPreviousTrack, btnNextTrack;
    private HorseBetAdapter adapter;
    private List<HorseBet> horseBets = new ArrayList<>();
    private Random random = new Random();
    
    // Music manager
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        vpHorses = findViewById(R.id.vpHorses);
        btnConfirmBet = findViewById(R.id.btnConfirmBet);
        btnBackToRace = findViewById(R.id.btnBackToRace);
        tvUserBalance = findViewById(R.id.tvUserBalance);
        btnToggleSound = findViewById(R.id.btnToggleSound);
        btnPreviousTrack = findViewById(R.id.btnPreviousTrack);
        btnNextTrack = findViewById(R.id.btnNextTrack);

        loadHorses();
        setupViewPager();
        updateBalanceDisplay();
        
        btnConfirmBet.setOnClickListener(v -> confirmBets());
        btnBackToRace.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        
        // Initialize music manager
        musicManager = new MusicManager(this, btnToggleSound);
        musicManager.bindMusicService();
        
        // Set up music controls
        setupMusicControls();
    }
    
    private void updateBalanceDisplay() {
        int balance = UserPreferences.getUserBalance(this);
        if (tvUserBalance != null) {
            tvUserBalance.setText("Balance: $" + balance);
        }
    }
    
    // Navigation is now handled by buttons instead of swipe detection
    
    private void setupMusicControls() {
        // Previous track button
        btnPreviousTrack.setOnClickListener(v -> {
            musicManager.previousTrack();
            showTrackChangeToast();
        });
        
        // Next track button
        btnNextTrack.setOnClickListener(v -> {
            musicManager.nextTrack();
            showTrackChangeToast();
        });
    }
    
    private void showTrackChangeToast() {
        String currentTrack = musicManager.getCurrentTrackName();
        if (currentTrack != null) {
            Toast.makeText(
                this, 
                getString(R.string.track_changed, currentTrack), 
                Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadHorses() {
        List<Horse> raceHorses = HorseRepository.getCurrentRaceHorses();
        horseBets.clear();
        
        // Check if raceHorses list is empty
        if (raceHorses == null || raceHorses.isEmpty()) {
            Toast.makeText(this, "No horses prepared for race", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Always ensure we have the latest race horses from the repository
        // This is important when a new race has been prepared
        
        // Check if we have existing bets from intent extras
        boolean hasExistingBets = getIntent().getBooleanExtra("has_existing_bets", false);
        
        // Generate odds for each horse
        for (Horse horse : raceHorses) {
            int horseId = horse.getId();
            float odds;
            
            // If we have existing bets for this horse, use the same odds
            if (hasExistingBets && getIntent().hasExtra("existing_bet_odds_" + horseId)) {
                odds = getIntent().getFloatExtra("existing_bet_odds_" + horseId, 0f);
            } else {
                // Generate new random odds between 1.5 and 5.0
                odds = 1.5f + random.nextFloat() * 3.5f;
                // Round to 1 decimal place
                odds = Math.round(odds * 10) / 10.0f;
            }
            
            HorseBet horseBet = new HorseBet(horse, odds);
            
            // If we have existing bets for this horse, set the previous bet amount
            if (hasExistingBets && getIntent().hasExtra("existing_bet_amount_" + horseId)) {
                int existingBetAmount = getIntent().getIntExtra("existing_bet_amount_" + horseId, 0);
                horseBet.setBetAmount(existingBetAmount);
                // Initialize newBetAmount as 0, user will add new bets
                horseBet.setNewBetAmount(0);
            }
            
            horseBets.add(horseBet);
        }
    }

    private void setupViewPager() {
        // Safety check for empty horse list
        if (horseBets == null || horseBets.isEmpty()) {
            Toast.makeText(this, "No horses available for betting", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        adapter = new HorseBetAdapter(this, horseBets);
        vpHorses.setAdapter(adapter);
        
        // Set page transformer for animation effect
        vpHorses.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            page.setAlpha(1 - absPosition * 0.5f);
            page.setScaleX(0.85f + (1 - absPosition) * 0.15f);
            page.setScaleY(0.85f + (1 - absPosition) * 0.15f);
        });
    }

    private void confirmBets() {
        boolean hasNewBets = false;
        int totalNewBetAmount = 0;
        StringBuilder betSummary = new StringBuilder("New bets:\n");
        
        // Calculate total new bet amount and create summary
        for (HorseBet bet : horseBets) {
            if (bet.getNewBetAmount() > 0) {
                hasNewBets = true;
                int amount = bet.getNewBetAmount();
                totalNewBetAmount += amount;
                
                betSummary.append(bet.getHorse().getName())
                          .append(": +$").append(amount)
                          .append(" (Odds: ").append(bet.getOdds()).append(")\n");
                
                if (bet.getBetAmount() > 0) {
                    // Include previous bet amount in summary
                    betSummary.append("  Previous bet: $").append(bet.getBetAmount())
                              .append(", Total: $").append(bet.getTotalBetAmount()).append("\n");
                }
            }
        }
        
        // Check if user has placed any new bets
        if (!hasNewBets) {
            Toast.makeText(this, "Please place at least one new bet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user has enough balance for new bets
        int userBalance = UserPreferences.getUserBalance(this);
        
        if (totalNewBetAmount > userBalance) {
            Toast.makeText(this, "Not enough balance to place these bets", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Deduct new bet amount from balance (actual winnings will be calculated after race)
        UserPreferences.subtractFromUserBalance(this, totalNewBetAmount);
        
        // Create intent to return data to RaceActivity
        Intent resultIntent = new Intent();
        
        // Add bet information to intent
        for (HorseBet bet : horseBets) {
            Horse horse = bet.getHorse();
            // Only send new bet data, RaceActivity will handle accumulation
            if (bet.getNewBetAmount() > 0) {
                resultIntent.putExtra("bet_amount_" + horse.getId(), bet.getNewBetAmount());
                resultIntent.putExtra("bet_odds_" + horse.getId(), bet.getOdds());
            }
        }
        
        // Set result and finish
        setResult(RESULT_OK, resultIntent);
        betSummary.append("\nTotal new bets: $").append(totalNewBetAmount);
        betSummary.append("\nNOTE: Bets are only valid for the current race. All bets will be discarded when a new race is prepared.");
        Toast.makeText(this, betSummary.toString(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog when back button is pressed
        new AlertDialog.Builder(this)
            .setTitle(R.string.exit_confirmation_title)
            .setMessage(R.string.exit_confirmation_message)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                // User cancelled betting, don't return any data
                setResult(RESULT_CANCELED);
                super.onBackPressed();
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                // If user cancels, do nothing
                dialog.dismiss();
            })
            .show();
    }
    
    @Override
    protected void onDestroy() {
        // Unbind from the music service when the activity is destroyed
        if (musicManager != null) {
            musicManager.unbindMusicService();
        }
        super.onDestroy();
    }
}