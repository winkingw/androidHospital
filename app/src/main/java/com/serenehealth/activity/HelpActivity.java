package com.serenehealth.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivityHelpBinding;

public class HelpActivity extends AppCompatActivity {

    private ActivityHelpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvContent.setText("挂号流程\n"
                + "1. 首页选择科室挂号。\n"
                + "2. 选择科室、医生、日期和可预约号源。\n"
                + "3. 提交预约后可在个人中心查看预约记录。\n\n"
                + "取消预约\n"
                + "进入个人中心-预约记录，找到已预约记录后点击取消预约。\n\n"
                + "自助缴费\n"
                + "进入首页自助缴费或个人中心缴费入口，可使用模拟支付或医保卡支付。");
    }
}
