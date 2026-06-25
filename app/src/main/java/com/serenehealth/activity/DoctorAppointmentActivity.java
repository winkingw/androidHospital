package com.serenehealth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.adapter.AdminAppointmentAdapter;
import com.serenehealth.bean.AdminUser;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorAppointmentDTO;
import com.serenehealth.databinding.ActivityDoctorAppointmentBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.List;

public class DoctorAppointmentActivity extends AppCompatActivity {
    private ActivityDoctorAppointmentBinding binding;
    private DBHelper dbHelper;
    private AdminAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = DBHelper.getInstance(this);

        long doctorId = SPUtil.getAdminDoctorId();
        if (doctorId <= 0) {
            Toast.makeText(this, "未关联医生账号", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initData(doctorId);
        loadDoctorHeader(doctorId);
        setListeners();
    }

    private void initData(long doctorId) {
        binding.rvAppointmentList.setLayoutManager(new LinearLayoutManager(this));
        List<DoctorAppointmentDTO> appointments = dbHelper.getAppointmentDao()
                .queryDoctorAppointmentsDetail(doctorId);

        if (appointments == null || appointments.isEmpty()) {
            binding.rvAppointmentList.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvAppointmentList.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            adapter = new AdminAppointmentAdapter(appointments, dbHelper);
            binding.rvAppointmentList.setAdapter(adapter);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadDoctorHeader(long doctorId) {
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
        binding.tvDoctorDepartment.setText(departmentName + "  " + title);
        binding.tvDoctorAccount.setText(username.isEmpty() ? "DOCTOR" : username);
    }
}
