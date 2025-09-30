package com.example.umamuse;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvBalance;
    CheckBox cb1, cb2, cb3;
    SeekBar sb1, sb2, sb3;
    EditText edtBet1, edtBet2, edtBet3;
    Button btnStart, btnReset;

    int balance = 100;
    Handler handler = new Handler();
    Random random = new Random();
    boolean isRacing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        tvBalance = findViewById(R.id.tvBalance);
        cb1 = findViewById(R.id.cb1);
        cb2 = findViewById(R.id.cb2);
        cb3 = findViewById(R.id.cb3);
        sb1 = findViewById(R.id.sb1);
        sb2 = findViewById(R.id.sb2);
        sb3 = findViewById(R.id.sb3);
        edtBet1 = findViewById(R.id.edtBet1);
        edtBet2 = findViewById(R.id.edtBet2);
        edtBet3 = findViewById(R.id.edtBet3);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);

        sb1.setEnabled(false);
        sb2.setEnabled(false);
        sb3.setEnabled(false);

        tvBalance.setText("Balance: " + balance + "$");

        btnStart.setOnClickListener(v -> startRace());
        btnReset.setOnClickListener(v -> resetRace());
    }

    private void startRace() {
        if (isRacing) return;
        isRacing = true;

        sb1.setProgress(0);
        sb2.setProgress(0);
        sb3.setProgress(0);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sb1.setProgress(sb1.getProgress() + random.nextInt(6));
                sb2.setProgress(sb2.getProgress() + random.nextInt(6));
                sb3.setProgress(sb3.getProgress() + random.nextInt(6));

                if (sb1.getProgress() >= sb1.getMax()) {
                    finishRace(1);
                } else if (sb2.getProgress() >= sb2.getMax()) {
                    finishRace(2);
                } else if (sb3.getProgress() >= sb3.getMax()) {
                    finishRace(3);
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void finishRace(int winner) {
        isRacing = false;
        int totalWin = 0;
        int totalLose = 0;

        // Lấy tiền cược từng ngựa
        int bet1 = getBetValue(edtBet1);
        int bet2 = getBetValue(edtBet2);
        int bet3 = getBetValue(edtBet3);

        // Tính thắng thua
        if (cb1.isChecked()) {
            if (winner == 1) {
                balance += bet1;
                totalWin += bet1;
            } else {
                balance -= bet1;
                totalLose += bet1;
            }
        }
        if (cb2.isChecked()) {
            if (winner == 2) {
                balance += bet2;
                totalWin += bet2;
            } else {
                balance -= bet2;
                totalLose += bet2;
            }
        }
        if (cb3.isChecked()) {
            if (winner == 3) {
                balance += bet3;
                totalWin += bet3;
            } else {
                balance -= bet3;
                totalLose += bet3;
            }
        }

        tvBalance.setText("Balance: " + balance + "$");

        // Tạo nội dung thông báo
        StringBuilder msg = new StringBuilder();
        msg.append("🐎 Ngựa số ").append(winner).append(" đã chiến thắng!\n\n");
        if (totalWin > 0) {
            msg.append("Bạn thắng: +").append(totalWin).append("$\n");
        }
        if (totalLose > 0) {
            msg.append("Bạn thua: -").append(totalLose).append("$\n");
        }
        if (totalWin == 0 && totalLose == 0) {
            msg.append("Bạn không đặt cược!\n");
        }
        msg.append("\nSố dư hiện tại: ").append(balance).append("$");

        // Hiển thị popup
        new AlertDialog.Builder(this)
                .setTitle("Kết quả cuộc đua 🎉")
                .setMessage(msg.toString())
                .setPositiveButton("OK", null)
                .show();
    }



    private int getBetValue(EditText edt) {
        try {
            return Integer.parseInt(edt.getText().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private void resetRace() {
        if (isRacing) return;
        sb1.setProgress(0);
        sb2.setProgress(0);
        sb3.setProgress(0);
        cb1.setChecked(false);
        cb2.setChecked(false);
        cb3.setChecked(false);
        edtBet1.setText("");
        edtBet2.setText("");
        edtBet3.setText("");
    }
}
