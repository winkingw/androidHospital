package com.serenehealth.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivityIdVerificationBinding;

public class IDVerificationActivity extends AppCompatActivity {

    private ActivityIdVerificationBinding binding;

    private boolean frontUploaded = false;
    private boolean backUploaded = false;
    private String verificationStatus = "PENDING";

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateStatusUI();
        setListeners();
    }

    private void setListeners() {
        binding.cardFront.setOnClickListener(v -> {
            if ("APPROVED".equals(verificationStatus)) {
                Toast.makeText(this, "审核已通过，无需重新上传", Toast.LENGTH_SHORT).show();
                return;
            }
            frontUploaded = true;
            binding.tvFrontLabel.setText("身份证正面 ✓");
            Toast.makeText(this, "已选择正面照片", Toast.LENGTH_SHORT).show();
            checkAutoSubmit();
        });

        binding.cardBack.setOnClickListener(v -> {
            if ("APPROVED".equals(verificationStatus)) {
                Toast.makeText(this, "审核已通过，无需重新上传", Toast.LENGTH_SHORT).show();
                return;
            }
            backUploaded = true;
            binding.tvBackLabel.setText("身份证反面 ✓");
            Toast.makeText(this, "已选择反面照片", Toast.LENGTH_SHORT).show();
            checkAutoSubmit();
        });

        binding.btnSubmit.setOnClickListener(v -> submitVerification());
    }

    private void checkAutoSubmit() {
        if (frontUploaded && backUploaded && "PENDING".equals(verificationStatus)) {
            binding.btnSubmit.setEnabled(true);
        }
    }

    private void submitVerification() {
        if (!frontUploaded || !backUploaded) {
            Toast.makeText(this, "请先上传身份证正反面照片", Toast.LENGTH_SHORT).show();
            return;
        }

        verificationStatus = "REVIEWING";
        updateStatusUI();

        Toast.makeText(this, "已提交审核，请耐心等待", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            verificationStatus = "APPROVED";
            updateStatusUI();
            Toast.makeText(this, "身份证核验通过！", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void updateStatusUI() {
        switch (verificationStatus) {
            case "PENDING":
                binding.tvVerificationStatus.setText(getString(com.serenehealth.R.string.id_status_pending));
                binding.tvVerificationStatus.setTextColor(getColor(com.serenehealth.R.color.on_surface_variant));
                binding.progressBar.setVisibility(View.GONE);
                binding.cardFront.setEnabled(true);
                binding.cardBack.setEnabled(true);
                binding.btnSubmit.setEnabled(frontUploaded && backUploaded);
                break;

            case "REVIEWING":
                binding.tvVerificationStatus.setText(getString(com.serenehealth.R.string.id_status_reviewing));
                binding.tvVerificationStatus.setTextColor(getColor(com.serenehealth.R.color.primary_container));
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.cardFront.setEnabled(false);
                binding.cardBack.setEnabled(false);
                binding.btnSubmit.setEnabled(false);
                break;

            case "APPROVED":
                binding.tvVerificationStatus.setText(getString(com.serenehealth.R.string.id_status_approved));
                binding.tvVerificationStatus.setTextColor(getColor(com.serenehealth.R.color.primary));
                binding.progressBar.setVisibility(View.GONE);
                binding.cardFront.setEnabled(false);
                binding.cardBack.setEnabled(false);
                binding.btnSubmit.setEnabled(false);
                break;
        }
    }
}
