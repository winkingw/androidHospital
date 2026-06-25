package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.bean.AdminUser;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.databinding.ActivityAdminMainBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = DBHelper.getInstance(this);

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
            binding.doctorHeaderCard.setVisibility(View.GONE);
            binding.cardAdminDepartment.setVisibility(View.VISIBLE);
            binding.cardAdminDoctor.setVisibility(View.VISIBLE);
            binding.cardAdminSchedule.setVisibility(View.VISIBLE);
            binding.cardAdminBanner.setVisibility(View.VISIBLE);
            binding.cardDoctorAppointments.setVisibility(View.GONE);
        } else if ("DOCTOR".equals(role)) {
            binding.doctorHeaderCard.setVisibility(View.VISIBLE);
            binding.cardAdminDepartment.setVisibility(View.GONE);
            binding.cardAdminDoctor.setVisibility(View.GONE);
            binding.cardAdminSchedule.setVisibility(View.GONE);
            binding.cardAdminBanner.setVisibility(View.GONE);
            binding.cardDoctorAppointments.setVisibility(View.VISIBLE);
            loadDoctorHeader();
        }
    }

    private void loadDoctorHeader() {
        long doctorId = SPUtil.getAdminDoctorId();
        Doctor doctor = dbHelper.getDoctorDao().queryDoctorById(doctorId);
        AdminUser account = dbHelper.getAdminUserDao().queryByDoctorId(doctorId);
        if (doctor == null) {
            binding.tvDoctorName.setText("医生账号");
            binding.tvDoctorDepartment.setText("未找到医生信息");
            binding.tvDoctorAccount.setText("DOCTOR");
            return;
        }

        Department department = dbHelper.getDepartmentDao()
                .queryDepartmentById(doctor.getDepartmentId());
        String departmentName = department != null ? department.getDeptName() : "";
        String title = doctor.getTitle() != null ? doctor.getTitle() : "";
        String username = account != null ? account.getUsername() : "";

        binding.tvDoctorName.setText(doctor.getDoctorName());
        binding.tvDoctorDepartment.setText((departmentName + "  " + title).trim());
        binding.tvDoctorAccount.setText(username.isEmpty() ? "DOCTOR" : username);
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
