package com.example.umamuse;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DepositMoneyActivity extends AppCompatActivity {
    Button btnTrolai, btnClickMua;
    RadioGroup radioBtn_group;
    RadioButton radBtn1, radBtn2, radBtn3;

    int selectedValue = 0;
    String selectedText = "";


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

        btnClickMua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int idSelect = radioBtn_group.getCheckedRadioButtonId();
                if (idSelect == -1) {
                    Toast.makeText(DepositMoneyActivity.this, "Vui lòng chọn gói tiền", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (idSelect == R.id.radBtn1) {
                    selectedValue = 100;
                    selectedText = "Starter Pack – 100 xu";
                } else if (idSelect == R.id.radBtn2) {
                    selectedValue = 550;
                    selectedText = "Pro Pack – 550 xu";
                } else if (idSelect == R.id.radBtn3) {
                    selectedValue = 1000;
                    selectedText = "Elite Pack – 1000 xu";
                }

                // Hiển thị hộp thoại xác nhận
                new AlertDialog.Builder(DepositMoneyActivity.this)
                        .setTitle("Xác nhận mua gói")
                        .setMessage("Bạn có chắc muốn mua " + selectedText + " không?")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent myItent = new Intent(DepositMoneyActivity.this, MainActivity.class);

                                // Lưu SharedPreferences khi người dùng chọn Đồng ý
                                SharedPreferences mySharedPreferences = getSharedPreferences("Money", MODE_PRIVATE);
                                SharedPreferences.Editor editor = mySharedPreferences.edit();

                                int oldBalance = mySharedPreferences.getInt("balance", 0); // số dư cũ

//                                if (oldBalance < selectedValue) {
//                                    Toast.makeText(Deposit_Money.this, "Số dư không đủ", Toast.LENGTH_LONG).show();
//                                    return;
//                                }

                                int newBalance = oldBalance + selectedValue;

                                editor.putInt("balance", newBalance); // lưu lại số dư
//                                editor.remove("balance"); // xóa dữ liệu cũ"

                                editor.commit();

                                Toast.makeText(DepositMoneyActivity.this, "Mua gói thành công: " + selectedText, Toast.LENGTH_LONG).show();
                                myItent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // xóa lịch sử activity trước đó
                                startActivity(myItent);
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

                finish(); // chỉ đơn giản quay lại màn hình trước
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
    protected void onPause() {
        super.onPause();

        SharedPreferences mySharedPreferences = getSharedPreferences("Money", MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt("selected_package", selectedValue); // lưu giá trị đã chọn
        editor.commit();
    }

}
