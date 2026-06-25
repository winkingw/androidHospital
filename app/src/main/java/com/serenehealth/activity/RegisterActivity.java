package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivityRegisterBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DBHelper dbHelper;
    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnLogin.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        if (submitting) {
            return;
        }
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String realName = binding.etRealName.getText().toString().trim();

        if (!phone.matches("^1\\d{10}$")) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (realName.isEmpty()) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        submitting = true;
        binding.btnRegister.setEnabled(false);
        long userId = dbHelper.getUserDao().register(phone, password, realName);
        if (userId > 0) {
            SPUtil.setLoggedIn(true);
            SPUtil.setCurrentUserId(userId);
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            submitting = false;
            binding.btnRegister.setEnabled(true);
            Toast.makeText(this, "手机号已注册", Toast.LENGTH_SHORT).show();
        }
    }
}
