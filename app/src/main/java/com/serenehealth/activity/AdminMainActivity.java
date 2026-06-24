package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivityAdminMainBinding;
import com.serenehealth.util.SPUtil;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String role = SPUtil.getAdminRole();
        if (role == null || role.isEmpty()) {
            // Not logged in, go back to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initData(role);
        setListeners(role);
    }

    private void initData(String role) {
        if ("ADMIN".equals(role)) {
            binding.cardAdminDepartment.setVisibility(View.VISIBLE);
            binding.cardAdminDoctor.setVisibility(View.VISIBLE);
            binding.cardAdminSchedule.setVisibility(View.VISIBLE);
            binding.cardAdminBanner.setVisibility(View.VISIBLE);
            binding.cardDoctorAppointments.setVisibility(View.GONE);
        } else if ("DOCTOR".equals(role)) {
            binding.cardAdminDepartment.setVisibility(View.GONE);
            binding.cardAdminDoctor.setVisibility(View.GONE);
            binding.cardAdminSchedule.setVisibility(View.GONE);
            binding.cardAdminBanner.setVisibility(View.GONE);
            binding.cardDoctorAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners(String role) {
        if ("ADMIN".equals(role)) {
            binding.cardAdminDepartment.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageDepartmentActivity.class)));
            binding.cardAdminDoctor.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageDoctorActivity.class)));
            binding.cardAdminSchedule.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageScheduleActivity.class)));
            binding.cardAdminBanner.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageBannerActivity.class)));
        }

        // Doctor appointments - visible for both ADMIN and DOCTOR
        binding.cardDoctorAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorAppointmentActivity.class)));

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnLogout.setOnClickListener(v -> {
            SPUtil.clearAdmin();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
