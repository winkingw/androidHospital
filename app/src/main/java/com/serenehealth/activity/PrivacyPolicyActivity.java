package com.serenehealth.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvContent.setText("Serene Health 隐私政策\n\n"
                + "本课程项目仅在本机 SQLite 数据库中保存模拟用户信息、预约记录、缴费订单和医保卡信息。"
                + "应用不接入真实医院后端，不上传身份证号、手机号或医保卡号。\n\n"
                + "个人信息仅用于本地演示登录、预约、缴费、评价和身份二维码展示。"
                + "二维码为本地虚拟码，不具备真实身份核验能力。");
    }
}
