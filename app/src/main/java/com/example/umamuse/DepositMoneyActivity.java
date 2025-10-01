package com.example.umamuse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.umamuse.utils.MusicManager;
import com.example.umamuse.utils.UserPreferences;

public class DepositMoneyActivity extends AppCompatActivity {
    private Button btnTrolai, btnClickMua;
    private RadioGroup radioBtn_group;
    private RadioButton radBtn1, radBtn2, radBtn3;
    private TextView tvCurrentBalance;
    private ToggleButton btnToggleSound;
    private Button btnPreviousTrack, btnNextTrack;
    
    // Music manager
    private MusicManager musicManager;

    private int selectedValue = 0;
    private String selectedText = "";
    
    // Define the package amounts
    private static final int PACK_1_AMOUNT = 100;
    private static final int PACK_2_AMOUNT = 550;
    private static final int PACK_3_AMOUNT = 1200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.deposit_money);

        btnTrolai = findViewById(R.id.btnTrolai);
        btnClickMua = findViewById(R.id.btnClickMua);
        radioBtn_group = findViewById(R.id.radioBtn_group);
        radBtn1 = findViewById(R.id.radBtn1);
        radBtn2 = findViewById(R.id.radBtn2);
        radBtn3 = findViewById(R.id.radBtn3);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        btnToggleSound = findViewById(R.id.btnToggleSound);
        btnPreviousTrack = findViewById(R.id.btnPreviousTrack);
        btnNextTrack = findViewById(R.id.btnNextTrack);
        
        // Display current balance
        updateBalanceDisplay();
        
        // Initialize music manager
        musicManager = new MusicManager(this, btnToggleSound);
        musicManager.bindMusicService();
        
        // Set up music controls
        setupMusicControls();

        btnClickMua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int idSelect = radioBtn_group.getCheckedRadioButtonId();
                if (idSelect == -1) {
                    Toast.makeText(DepositMoneyActivity.this, "Vui lòng chọn gói tiền", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (idSelect == R.id.radBtn1) {
                    selectedValue = PACK_1_AMOUNT;
                    selectedText = "Return Pack – $" + PACK_1_AMOUNT;
                } else if (idSelect == R.id.radBtn2) {
                    selectedValue = PACK_2_AMOUNT;
                    selectedText = "Comeback Pack – $" + PACK_2_AMOUNT;
                } else if (idSelect == R.id.radBtn3) {
                    selectedValue = PACK_3_AMOUNT;
                    selectedText = "God of Bet Pack – $" + PACK_3_AMOUNT;
                }

                // Hiển thị hộp thoại xác nhận
                new AlertDialog.Builder(DepositMoneyActivity.this)
                        .setTitle("Xác nhận mua gói")
                        .setMessage("Bạn có chắc muốn mua " + selectedText + " không?")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Add money to user's balance using UserPreferences
                                UserPreferences.addToUserBalance(DepositMoneyActivity.this, selectedValue);
                                
                                // Update balance display
                                updateBalanceDisplay();
                                
                                // Show success animation on button
                                btnClickMua.animate()
                                    .scaleX(0.9f)
                                    .scaleY(0.9f)
                                    .setDuration(100)
                                    .withEndAction(() -> {
                                        btnClickMua.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100);
                                    });

                                // Show success message
                                Toast.makeText(DepositMoneyActivity.this, 
                                    "Nạp tiền thành công: " + selectedText, 
                                    Toast.LENGTH_LONG).show();
                                
                                // Return to RaceActivity
                                Intent raceIntent = new Intent(DepositMoneyActivity.this, RaceActivity.class);
                                raceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(raceIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(DepositMoneyActivity.this, "Bạn đã hủy giao dịch", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        btnTrolai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog when back button is pressed
                new AlertDialog.Builder(DepositMoneyActivity.this)
                    .setTitle(R.string.exit_confirmation_title)
                    .setMessage(R.string.exit_confirmation_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        // If user confirms, navigate back
                        finish();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        // If user cancels, do nothing
                        dialog.dismiss();
                    })
                    .show();
            }
        });

        // Đọc lại dữ liệu đã lưu trong SharedPreferences

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalanceDisplay();
    }
    
    @Override
    protected void onDestroy() {
        // Unbind from the music service when the activity is destroyed
        if (musicManager != null) {
            musicManager.unbindMusicService();
        }
        super.onDestroy();
    }
    
    /**
     * Set up music control buttons
     */
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
    
    /**
     * Hiển thị thông báo khi thay đổi bài hát
     */
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
    
    /**
     * Updates the balance display TextView with the current user balance
     */
    private void updateBalanceDisplay() {
        if (tvCurrentBalance != null) {
            int currentBalance = UserPreferences.getUserBalance(this);
            tvCurrentBalance.setText("Your current balance: $" + currentBalance);
        }
    }
}
