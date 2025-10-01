package com.example.umamuse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS = "uma_prefs";
    private static final String KEY_NAME = "playerName";

    private EditText edtPlayerName;
    private CheckBox cbRemember;
    private LinearLayout loginForm;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ĐÃ ĐỔI: dùng layout mới cho màn Login
        setContentView(R.layout.activity_login);

        // Bind view theo id trong activity_login.xml
        edtPlayerName = findViewById(R.id.edtPlayerName);
        cbRemember    = findViewById(R.id.cbRemember);
        loginForm     = findViewById(R.id.loginForm);
        tvGreeting    = findViewById(R.id.tvGreeting);
        Button btnStart = findViewById(R.id.btnStart);

        // Auto-fill nếu đã nhớ tên
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = sp.getString(KEY_NAME, null);
        if (saved != null && !saved.isEmpty()) {
            showGreeting(saved);
        }

        // Click nút bắt đầu
        btnStart.setOnClickListener(v -> tryLogin());

        // Nhấn Enter trên bàn phím để submit
        edtPlayerName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                tryLogin();
                return true;
            }
            return false;
        });
    }

    private void tryLogin() {
        String name = edtPlayerName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtPlayerName.setError("Nhập tên để vào sảnh đua nha!");
            edtPlayerName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            edtPlayerName.setError("Tên phải ≥ 2 ký tự");
            edtPlayerName.requestFocus();
            return;
        }

        if (cbRemember.isChecked()) {
            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_NAME, name)
                    .apply();
        }

        Toast.makeText(this, "Welcome, Trainer " + name + "!", Toast.LENGTH_SHORT).show();
        showGreeting(name);
    }

    private void showGreeting(String name) {
        String msg = "Chào mừng, Huấn luyện viên " + name + "!\nChuẩn bị xuất phát nào!";
        tvGreeting.setText(msg);
        tvGreeting.setVisibility(View.VISIBLE);
        loginForm.setVisibility(View.GONE);
        
        // Navigate to OpeningActivity after a short delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, OpeningActivity.class);
                startActivity(intent);
                finish(); // Finish LoginActivity so user can't go back to it
            }
        }, 2000); // 2 seconds delay
    }
}
