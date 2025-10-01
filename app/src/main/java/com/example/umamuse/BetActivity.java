package com.example.umamuse;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.umamuse.adapters.HorseBetAdapter;
import com.example.umamuse.models.Horse;
import com.example.umamuse.models.HorseBet;
import com.example.umamuse.repositories.HorseRepository;
import com.example.umamuse.utils.UserPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BetActivity extends AppCompatActivity {

    private ViewPager2 vpHorses;
    private Button btnConfirmBet, btnBackToRace;
    private TextView tvUserBalance;
    private HorseBetAdapter adapter;
    private List<HorseBet> horseBets = new ArrayList<>();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        vpHorses = findViewById(R.id.vpHorses);
        btnConfirmBet = findViewById(R.id.btnConfirmBet);
        btnBackToRace = findViewById(R.id.btnBackToRace);
        tvUserBalance = findViewById(R.id.tvUserBalance);

        loadHorses();
        setupViewPager();
        updateBalanceDisplay();
        
        btnConfirmBet.setOnClickListener(v -> confirmBets());
        btnBackToRace.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
        finish();
});
    }
    
    private void updateBalanceDisplay() {
        int balance = UserPreferences.getUserBalance(this);
        if (tvUserBalance != null) {
            tvUserBalance.setText("Balance: $" + balance);
        }
    }
    
    // Navigation is now handled by buttons instead of swipe detection

    private void loadHorses() {
        List<Horse> raceHorses = HorseRepository.getCurrentRaceHorses();
        horseBets.clear();
        
        // Check if raceHorses list is empty
        if (raceHorses == null || raceHorses.isEmpty()) {
            Toast.makeText(this, "No horses prepared for race", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Generate random odds for each horse
        for (Horse horse : raceHorses) {
            // Odds between 1.5 and 5.0
            float odds = 1.5f + random.nextFloat() * 3.5f;
            // Round to 1 decimal place
            odds = Math.round(odds * 10) / 10.0f;
            horseBets.add(new HorseBet(horse, odds));
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
        boolean hasBets = false;
        int totalBetAmount = 0;
        StringBuilder betSummary = new StringBuilder("Your bets:\n");
        
        // Calculate total bet amount and create summary
        for (HorseBet bet : horseBets) {
            if (bet.getBetAmount() > 0) {
                hasBets = true;
                int amount = bet.getBetAmount();
                totalBetAmount += amount;
                
                betSummary.append(bet.getHorse().getName())
                          .append(": $").append(amount)
                          .append(" (Odds: ").append(bet.getOdds()).append(")\n");
            }
        }
        
        // Check if user has enough balance
        int userBalance = UserPreferences.getUserBalance(this);
        
        if (hasBets) {
            if (totalBetAmount > userBalance) {
                Toast.makeText(this, "Not enough balance to place these bets", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Deduct bet amount from balance (actual winnings will be calculated after race)
            UserPreferences.subtractFromUserBalance(this, totalBetAmount);
            
            // Create intent to return data to RaceActivity
            Intent resultIntent = new Intent();
            
            // Add bet information to intent
            for (HorseBet bet : horseBets) {
                if (bet.getBetAmount() > 0) {
                    Horse horse = bet.getHorse();
                    resultIntent.putExtra("bet_amount_" + horse.getId(), bet.getBetAmount());
                    resultIntent.putExtra("bet_odds_" + horse.getId(), bet.getOdds());
                }
            }
            
            // Set result and finish
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, betSummary.toString(), Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Please place at least one bet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // User cancelled betting, don't return any data
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}