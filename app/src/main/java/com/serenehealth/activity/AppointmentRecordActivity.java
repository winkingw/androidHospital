package com.serenehealth.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.R;
import com.serenehealth.adapter.AppointmentRecordAdapter;
import com.serenehealth.bean.Appointment;
import com.serenehealth.databinding.ActivityAppointmentRecordBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.List;

public class AppointmentRecordActivity extends AppCompatActivity {

    private ActivityAppointmentRecordBinding binding;
    private DBHelper dbHelper;
    private AppointmentRecordAdapter adapter;
    private String currentStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
        loadAppointments();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        adapter = new AppointmentRecordAdapter(dbHelper, new AppointmentRecordAdapter.OnAppointmentActionListener() {
            @Override
            public void onCancel(Appointment appointment) {
                confirmCancel(appointment);
            }

            @Override
            public void onFeedback(Appointment appointment) {
                Intent intent = new Intent(AppointmentRecordActivity.this, FeedbackActivity.class);
                intent.putExtra("appointment_id", appointment.getId());
                startActivity(intent);
            }
        });
        binding.rvAppointmentRecord.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointmentRecord.setAdapter(adapter);
        binding.groupFilter.check(R.id.btn_filter_all);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.groupFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_filter_booked) {
                currentStatus = "BOOKED";
            } else if (checkedId == R.id.btn_filter_visited) {
                currentStatus = "VISITED";
            } else if (checkedId == R.id.btn_filter_canceled) {
                currentStatus = "CANCELED";
            } else {
                currentStatus = "";
            }
            loadAppointments();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && adapter != null) {
            loadAppointments();
        }
    }

    private void loadAppointments() {
        long userId = SPUtil.getCurrentUserId();
        if (userId <= 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        List<Appointment> appointments;
        if (currentStatus == null || currentStatus.isEmpty()) {
            appointments = dbHelper.getAppointmentDao().queryAppointmentsByUser(userId);
        } else {
            appointments = dbHelper.getAppointmentDao().queryAppointmentsByStatus(userId, currentStatus);
        }
        adapter.setData(appointments);
        boolean empty = appointments == null || appointments.isEmpty();
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvAppointmentRecord.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmCancel(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.appointment_cancel)
                .setMessage("确定取消该预约吗？")
                .setPositiveButton(R.string.message_center_dialog_confirm, (dialog, which) -> {
                    boolean success = dbHelper.getAppointmentDao().cancelAppointment(
                            appointment.getId(), getString(R.string.appointment_cancel_reason));
                    Toast.makeText(this, success ? "预约已取消" : "取消失败", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                })
                .setNegativeButton(R.string.action_back, null)
                .show();
    }
}
