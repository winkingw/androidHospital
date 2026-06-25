package com.serenehealth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.adapter.AdminAppointmentAdapter;
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
            adapter = new AdminAppointmentAdapter(appointments);
            binding.rvAppointmentList.setAdapter(adapter);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }
}
