package com.example.umamuse;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.umamuse.models.Horse;
import com.example.umamuse.models.HorseBet;
import com.example.umamuse.repositories.HorseRepository;
import com.example.umamuse.utils.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RaceActivity extends AppCompatActivity {

    private ImageView bgImage1, bgImage2;
    private View finishLine;
    private Button btnPrepare, btnStart, btnPlaceBet;
    private FrameLayout lane1, lane2, lane3, lane4;

    private List<Horse> raceHorses;
    private List<ImageView> horseImages;
    private List<SeekBar> horseSeekBars;

    private Handler handler = new Handler();
    private Random random = new Random();
    private int trackWidth;

    private int lapCount = 0;
    private final int TOTAL_LAPS = 5;
    private float totalDistance = 0;
    private float lapDistance = 0;

    private boolean raceRunning = false;
    private Horse winnerHorse = null;
    private float[] horseYOffsets;

    private Runnable raceRunnable;
    private float touchX1, touchX2;
    private static final float MIN_DISTANCE = 150;
    private Map<Integer, HorseBet> userBets = new HashMap<>();
    private ActivityResultLauncher<Intent> betActivityLauncher;
    private TextView tvUserBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        bgImage1 = findViewById(R.id.bgImage1);
        bgImage2 = findViewById(R.id.bgImage2);
        finishLine = findViewById(R.id.finishLine);
        btnPrepare = findViewById(R.id.btnNewRace);
        btnStart = findViewById(R.id.btnStart);
        btnPlaceBet = findViewById(R.id.btnPlaceBet);
        tvUserBalance = findViewById(R.id.tvUserBalance);

        lane1 = findViewById(R.id.lane1);
        lane2 = findViewById(R.id.lane2);
        lane3 = findViewById(R.id.lane3);
        lane4 = findViewById(R.id.lane4);

        horseImages = new ArrayList<>();
        horseSeekBars = new ArrayList<>();

        btnPrepare.setText("Prepare Race");
        btnStart.setText("Start Race");
        btnStart.setEnabled(false);

        btnPrepare.setOnClickListener(v -> prepareRace());
        btnStart.setOnClickListener(v -> startRace());
        btnPlaceBet.setOnClickListener(v -> openBetActivity());
        
        // Initially disable the bet button until horses are prepared
        btnPlaceBet.setEnabled(false);
        
        // Set up activity result launcher for BetActivity
        setupBetActivityLauncher();
        
        // Display user balance
        updateUserBalanceDisplay();
    }
    
    // This method is no longer needed as we're using buttons instead of swipe detection
    // Keeping the method signature empty for compatibility
    private void setupSwipeDetection() {
        // Navigation is now handled by buttons instead of swipe detection
    }
    
    private void setupBetActivityLauncher() {
        betActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get bets data from BetActivity
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        // Process new bets and accumulate them with existing ones
                        for (Horse horse : raceHorses) {
                            int horseId = horse.getId();
                            if (extras.containsKey("bet_amount_" + horseId) && extras.containsKey("bet_odds_" + horseId)) {
                                int newBetAmount = extras.getInt("bet_amount_" + horseId);
                                float odds = extras.getFloat("bet_odds_" + horseId);
                                
                                if (newBetAmount > 0) {
                                    // If we already have a bet for this horse, add to it
                                    if (userBets.containsKey(horseId)) {
                                        HorseBet existingBet = userBets.get(horseId);
                                        // Keep the average of the odds weighted by bet amounts
                                        int existingAmount = existingBet.getBetAmount();
                                        float existingOdds = existingBet.getOdds();
                                        
                                        int totalAmount = existingAmount + newBetAmount;
                                        // Calculate weighted average odds
                                        float weightedOdds = (existingOdds * existingAmount + odds * newBetAmount) / totalAmount;
                                        // Round to 1 decimal place
                                        weightedOdds = Math.round(weightedOdds * 10) / 10.0f;
                                        
                                        existingBet.setBetAmount(totalAmount);
                                        existingBet.setOdds(weightedOdds);
                                    } else {
                                        // Create a new bet for this horse
                                        userBets.put(horseId, new HorseBet(horse, odds));
                                        userBets.get(horseId).setBetAmount(newBetAmount);
                                    }
                                }
                            }
                        }
                        updateUserBalanceDisplay();
                        
                        // Show summary of accumulated bets
                        showAccumulatedBetsSummary();
                    }
                }
            }
        );
    }

    private void updateUserBalanceDisplay() {
        if (tvUserBalance != null) {
            int balance = UserPreferences.getUserBalance(this);
            tvUserBalance.setText("Balance: $" + balance);
        }
    }
    
    private void showAccumulatedBetsSummary() {
        if (userBets.isEmpty()) {
            return;
        }
        
        int totalBetAmount = 0;
        StringBuilder summary = new StringBuilder("Current bets:\n");
        
        for (HorseBet bet : userBets.values()) {
            if (bet.getBetAmount() > 0) {
                totalBetAmount += bet.getBetAmount();
                summary.append(bet.getHorse().getName())
                       .append(": $").append(bet.getBetAmount())
                       .append(" (Odds: ").append(bet.getOdds()).append(")\n");
            }
        }
        
        summary.append("\nTotal bet amount: $").append(totalBetAmount);
        Toast.makeText(this, summary.toString(), Toast.LENGTH_LONG).show();
    }
    
    private void openBetActivity() {
        // Make sure we have horses prepared before opening the bet activity
        if (raceHorses == null || raceHorses.isEmpty()) {
            Toast.makeText(this, "Please prepare the race first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Ensure race horses are set in the repository
        HorseRepository.setCurrentRaceHorses(raceHorses);
        
        Intent intent = new Intent(this, BetActivity.class);
        
        // Pass current bet information to BetActivity
        boolean hasExistingBets = !userBets.isEmpty();
        
        if (hasExistingBets) {
            intent.putExtra("has_existing_bets", true);
            
            for (Map.Entry<Integer, HorseBet> entry : userBets.entrySet()) {
                int horseId = entry.getKey();
                HorseBet bet = entry.getValue();
                
                intent.putExtra("existing_bet_amount_" + horseId, bet.getBetAmount());
                intent.putExtra("existing_bet_odds_" + horseId, bet.getOdds());
            }
        } else {
            intent.putExtra("has_existing_bets", false);
        }
        
        betActivityLauncher.launch(intent);
    }

    private void prepareRace() {
        // --- Chọn ngựa random ---
        List<Horse> allHorses = HorseRepository.getAllHorses();
        Collections.shuffle(allHorses);
        raceHorses = new ArrayList<>(allHorses.subList(0, 4));
        HorseRepository.setCurrentRaceHorses(raceHorses);

        // Reset bets for new race - completely discard all previous bets
        userBets.clear();

        // Clear UI elements
        horseImages.clear();
        horseSeekBars.clear();
        lane1.removeAllViews();
        lane2.removeAllViews();
        lane3.removeAllViews();
        lane4.removeAllViews();

        addHorseToLane(raceHorses.get(0), lane1);
        addHorseToLane(raceHorses.get(1), lane2);
        addHorseToLane(raceHorses.get(2), lane3);
        addHorseToLane(raceHorses.get(3), lane4);

        horseYOffsets = new float[raceHorses.size()];

        // Reset background
        bgImage1.setTranslationX(0);
        bgImage2.setTranslationX(bgImage1.getWidth());
        finishLine.setVisibility(View.GONE);

        // Reset trạng thái race
        raceRunning = false;
        lapCount = 0;
        totalDistance = 0;
        winnerHorse = null;

        // Enable Start and Place Bet buttons
        btnStart.setEnabled(true);
        btnPlaceBet.setEnabled(true);
        btnPrepare.setEnabled(false);
        
        // Show that all bets have been cleared for the new race
        Toast.makeText(this, "New race prepared with fresh horses. All previous bets have been discarded. Place new bets!", Toast.LENGTH_LONG).show();
    }

    private void startRace() {
        raceRunning = true;
        btnStart.setEnabled(false);
        btnPrepare.setEnabled(false);
        btnPlaceBet.setEnabled(false);

        // Đảm bảo trackWidth đã đo xong
        lane1.post(() -> {
            trackWidth = lane1.getWidth();
            lapDistance = trackWidth * 2;

            // Khởi tạo speed và SeekBar max
            for (int i = 0; i < raceHorses.size(); i++) {
                Horse h = raceHorses.get(i);
                h.setSpeed(2 + random.nextInt(3));
                h.setFinished(false);
                h.setForward(true);
                h.setLastDirectionChange(System.currentTimeMillis());
                h.setDirectionInterval(2000 + random.nextInt(1000));

                SeekBar sb = horseSeekBars.get(i);
                ImageView iv = horseImages.get(i);
                sb.setMax(trackWidth - iv.getWidth());
                sb.setProgress(0);
                iv.setTranslationX(0);
            }

            startRaceRunnable(); // chạy Runnable sau khi trackWidth có giá trị
        });
    }

    private void startRaceRunnable() {
        raceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!raceRunning) return;

                long now = System.currentTimeMillis();
                boolean allFinished = true;
                boolean showFinishLine = lapCount >= TOTAL_LAPS - 1;

                // Hiển thị finish line khi gần lap cuối
                if (showFinishLine && finishLine.getVisibility() == View.GONE) {
                    finishLine.setVisibility(View.VISIBLE);
                    if (winnerHorse == null)
                        winnerHorse = raceHorses.get(random.nextInt(raceHorses.size()));
                }

                for (int i = 0; i < raceHorses.size(); i++) {
                    Horse horse = raceHorses.get(i);
                    ImageView iv = horseImages.get(i);
                    SeekBar sb = horseSeekBars.get(i);

                    if (!horse.isFinished()) {
                        allFinished = false;

                        int delta = (int) horse.getSpeed() + random.nextInt(3) - 1;
                        int newPos = sb.getProgress();

                        // --- Smooth movement logic ---
                        if (!showFinishLine) {
                            // Lap > 0 thì ngẫu nhiên nhích qua trái/phải nhưng smooth
                            if (lapCount > 0) {
                                int mid = (int)(sb.getMax() * 0.6);
                                int buf = (int)(sb.getMax() * 0.2);

                                // Nếu gần biên trái/phải → giảm tốc và hướng forward
                                if (newPos < mid - buf) horse.setForward(true);
                                else if (newPos > mid + buf) horse.setForward(false);

                                if (newPos >= sb.getMax() - 5) delta = Math.min(delta, 2);
                                if (newPos <= 5) delta = Math.min(delta, 2);

                                newPos += horse.isForward() ? delta : -delta;
                                newPos = Math.max(0, Math.min(sb.getMax(), newPos));
                            } else {
                                // Lap 0 → đi thẳng
                                newPos = Math.min(sb.getMax(), newPos + delta);
                            }
                        } else {
                            // Finish line logic
                            if (horse == winnerHorse) {
                                newPos = Math.min(sb.getMax(), newPos + delta + 3);
                                if (newPos >= sb.getMax()) {
                                    horse.setFinished(true);
                                    if (raceRunning) {
                                        raceRunning = false;
                                        showRaceResultDialog(horse);
                                        btnPrepare.setEnabled(true);
                                    }
                                }
                            } else {
                                // Những ngựa còn lại đi chậm hơn
                                newPos = Math.min((int)(sb.getMax() * 0.9), newPos + delta - 1);
                            }
                        }

                        sb.setProgress(newPos);
                        iv.setTranslationX(newPos);

                        // --- Xóc vó ngựa ---
                        horseYOffsets[i] = (float) (Math.sin(System.currentTimeMillis()*0.01 + i)*5);
                        iv.setTranslationY(horseYOffsets[i]);
                    }
                }

                // --- Update background ---
                float speed = 10f;
                totalDistance += speed;
                if (lapDistance > 0) lapCount = (int)(totalDistance / lapDistance);

                float x1 = bgImage1.getTranslationX() - speed;
                float x2 = bgImage2.getTranslationX() - speed;
                bgImage1.setTranslationX(x1);
                bgImage2.setTranslationX(x2);

                if (x1 <= -bgImage1.getWidth()) bgImage1.setTranslationX(x2 + bgImage1.getWidth());
                if (x2 <= -bgImage2.getWidth()) bgImage2.setTranslationX(x1 + bgImage2.getWidth());

                if (raceRunning) handler.postDelayed(this, 16);
            }
        };

        handler.post(raceRunnable);
    }


    private void addHorseToLane(Horse horse, FrameLayout lane) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(horse.getImageRes());
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        iv.setLayoutParams(params);
        lane.addView(iv);
        horseImages.add(iv);

        SeekBar sb = new SeekBar(this);
        sb.setMax(0);
        sb.setProgress(0);
        sb.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,10));
        lane.addView(sb);
        horseSeekBars.add(sb);
    }

    private void showRaceResultDialog(Horse winner) {
        // Create custom dialog using the layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_race_result, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        // Initialize dialog views
        TextView tvWinner = dialogView.findViewById(R.id.tvWinner);
        LinearLayout layoutBets = dialogView.findViewById(R.id.layoutBets);
        TextView tvTotalBet = dialogView.findViewById(R.id.tvTotalBet);
        TextView tvTotalWinnings = dialogView.findViewById(R.id.tvTotalWinnings);
        TextView tvNewBalance = dialogView.findViewById(R.id.tvNewBalance);
        Button btnPrepareNewRace = dialogView.findViewById(R.id.btnPrepareNewRace);
        
        // Set winner name
        tvWinner.setText(winner.getName());
        
        // Calculate totals
    int totalBetAmount = 0;
    int totalWinnings = 0;
    
    // Add bet details to dialog
    for (HorseBet bet : userBets.values()) {
        Horse horse = bet.getHorse();
        int amount = bet.getBetAmount();
        if (amount > 0) {
            totalBetAmount += amount;
            
            TextView betView = new TextView(this);
            String betText = horse.getName() + ": $" + amount;
            
            // Check if this was the winning horse
            if (horse.getId() == winner.getId()) {
                int winnings = (int)(amount * bet.getOdds());
                totalWinnings += winnings;
                betText += " → $" + winnings + " (WON!)";
                betView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                betText += " (Lost)";
            }
            
            betView.setText(betText);
            betView.setTextSize(16);
            layoutBets.addView(betView);
        }
    }
    
    // Update user balance - DON'T deduct bet amount again, only add winnings
    int currentBalance = UserPreferences.getUserBalance(this);
    int newBalance = currentBalance + totalWinnings; // Bet was already deducted in BetActivity
    UserPreferences.setUserBalance(this, newBalance);
    
    // Set totals in dialog
    tvTotalBet.setText("$" + totalBetAmount);
    tvTotalWinnings.setText("$" + totalWinnings);
    tvNewBalance.setText("$" + newBalance);
        
        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        
        // Set button click listener for preparing a new race
        btnPrepareNewRace.setOnClickListener(v -> {
            dialog.dismiss();
            // Clear all previous bets
            userBets.clear();
            // Prepare a new race with completely new horses
            prepareRace();
            updateUserBalanceDisplay();
            Toast.makeText(this, "New race prepared with fresh horses! All previous bets have been discarded.", Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUserBalanceDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
