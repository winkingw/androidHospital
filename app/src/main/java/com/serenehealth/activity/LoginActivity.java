package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.bean.AdminUser;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityLoginBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.MockDataUtil;
import com.serenehealth.util.SPUtil;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database and mock data on first launch
        if (SPUtil.isFirstLaunch()) {
            dbHelper = DBHelper.getInstance(this);
            dbHelper.getWritableDatabase();
            MockDataUtil.initAll(dbHelper);
            SPUtil.setFirstLaunchDone();
        }

        // Check if already logged in
        if (SPUtil.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        if (SPUtil.isAdminLoggedIn()) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        dbHelper = DBHelper.getInstance(this);
        setListeners();
    }

    private void setListeners() {
        // Unified login: auto-detect user or admin
        binding.btnUserLogin.setOnClickListener(v -> {
            String account = binding.etAccount.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            // Try user login first
            User user = dbHelper.getUserDao().login(account, password);
            if (user != null) {
                SPUtil.setLoggedIn(true);
                SPUtil.setCurrentUserId(user.getId());
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
            // Try admin login second
            AdminUser adminUser = dbHelper.getAdminUserDao().login(account, password);
            if (adminUser != null) {
                SPUtil.setAdminRole(adminUser.getRoleType());
                SPUtil.setAdminDoctorId(adminUser.getDoctorId());
                startActivity(new Intent(this, AdminMainActivity.class));
                finish();
                return;
            }
            // Both failed
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        });
        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
