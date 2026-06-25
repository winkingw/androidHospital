package com.serenehealth.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityProfileDetailBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

public class ProfileDetailActivity extends AppCompatActivity {

    private ActivityProfileDetailBinding binding;
    private DBHelper dbHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        user = dbHelper.getUserDao().queryUserById(SPUtil.getCurrentUserId());
        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.etRealName.setText(user.getRealName());
        binding.etPhone.setText(user.getPhone());
        binding.etGender.setText(String.valueOf(user.getGender()));
        binding.etBirthDate.setText(user.getBirthDate());
        binding.etIdCard.setText(user.getIdCardNo());
        binding.etHealthScore.setText(String.valueOf(user.getHealthScore()));
        binding.etMemberLevel.setText(user.getMemberLevel());
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveUser());
    }

    private void saveUser() {
        String realName = binding.etRealName.getText().toString().trim();
        String genderText = binding.etGender.getText().toString().trim();
        String idCard = binding.etIdCard.getText().toString().trim();
        String healthScoreText = binding.etHealthScore.getText().toString().trim();

        if (realName.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!idCard.isEmpty() && !idCard.matches("^\\d{17}[0-9Xx]$")) {
            Toast.makeText(this, "身份证号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setRealName(realName);
        user.setGender(parseInt(genderText, 0));
        user.setBirthDate(binding.etBirthDate.getText().toString().trim());
        user.setIdCardNo(idCard.isEmpty() ? null : idCard);
        user.setHealthScore(parseInt(healthScoreText, 0));
        user.setMemberLevel(binding.etMemberLevel.getText().toString().trim());

        int rows = dbHelper.getUserDao().updateUser(user);
        Toast.makeText(this, rows > 0 ? "保存成功" : "保存失败", Toast.LENGTH_SHORT).show();
        if (rows > 0) {
            finish();
        }
    }

    private int parseInt(String text, int defValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }
}
