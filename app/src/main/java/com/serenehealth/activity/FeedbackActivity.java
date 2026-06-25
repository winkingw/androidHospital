package com.serenehealth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.adapter.FeedbackAppointmentAdapter;
import com.serenehealth.bean.Appointment;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.Feedback;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.databinding.ActivityFeedbackBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;
    private DBHelper dbHelper;
    private FeedbackAppointmentAdapter adapter;
    private Appointment appointment;
    private long doctorId;
    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        adapter = new FeedbackAppointmentAdapter(dbHelper, this::selectAppointment);
        binding.rvAppointment.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointment.setAdapter(adapter);
        loadAppointments();

        long appointmentId = getIntent().getLongExtra("appointment_id", -1);
        if (appointmentId > 0) {
            Appointment target = dbHelper.getAppointmentDao().queryAppointmentDetail(appointmentId);
            if (target != null && "VISITED".equals(target.getAppointmentStatus())) {
                selectAppointment(target);
            }
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        if (submitting) {
            return;
        }
        int doctorScore = (int) binding.rbDoctor.getRating();
        int serviceScore = (int) binding.rbService.getRating();
        int visitScore = (int) binding.rbVisit.getRating();
        if (doctorScore == 0 || serviceScore == 0 || visitScore == 0) {
            Toast.makeText(this, "请选择完整评分", Toast.LENGTH_SHORT).show();
            return;
        }
        if (appointment == null) {
            Toast.makeText(this, "请先选择预约单", Toast.LENGTH_SHORT).show();
            return;
        }
        doctorId = resolveDoctorId(appointment);
        if (doctorId <= 0) {
            Toast.makeText(this, "未找到预约对应医生", Toast.LENGTH_SHORT).show();
            return;
        }

        submitting = true;
        binding.btnSubmit.setEnabled(false);
        Feedback feedback = new Feedback();
        feedback.setUserId(SPUtil.getCurrentUserId());
        feedback.setAppointmentId(appointment.getId());
        feedback.setDoctorId(doctorId);
        feedback.setDoctorScore(doctorScore);
        feedback.setServiceScore(serviceScore);
        feedback.setVisitScore(visitScore);
        feedback.setContent(binding.etContent.getText().toString().trim());
        Feedback existing = dbHelper.getFeedbackDao().queryByAppointmentId(appointment.getId());
        boolean success;
        if (existing == null) {
            success = dbHelper.getFeedbackDao().insert(feedback) > 0;
        } else {
            success = dbHelper.getFeedbackDao().updateByAppointmentId(feedback) > 0;
        }
        Toast.makeText(this, success ? "评价已提交" : "提交失败", Toast.LENGTH_SHORT).show();
        if (success) {
            finish();
        } else {
            submitting = false;
            binding.btnSubmit.setEnabled(true);
        }
    }

    private void loadAppointments() {
        List<Appointment> appointments = dbHelper.getAppointmentDao()
                .queryAppointmentsByStatus(SPUtil.getCurrentUserId(), "VISITED");
        adapter.setData(appointments);
        boolean empty = appointments == null || appointments.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvAppointment.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void selectAppointment(Appointment selected) {
        appointment = selected;
        doctorId = resolveDoctorId(selected);
        Feedback existing = dbHelper.getFeedbackDao().queryByAppointmentId(selected.getId());
        binding.cardFeedbackForm.setVisibility(View.VISIBLE);
        binding.tvAppointmentInfo.setText(resolveTitle(selected) + "\n预约编号：" + selected.getAppointmentNo());
        if (existing == null) {
            binding.rbDoctor.setRating(5);
            binding.rbService.setRating(5);
            binding.rbVisit.setRating(5);
            binding.etContent.setText("");
            binding.btnSubmit.setText("提交评价");
        } else {
            binding.rbDoctor.setRating(existing.getDoctorScore());
            binding.rbService.setRating(existing.getServiceScore());
            binding.rbVisit.setRating(existing.getVisitScore());
            binding.etContent.setText(existing.getContent());
            binding.btnSubmit.setText("保存修改");
        }
        binding.btnSubmit.setEnabled(true);
        submitting = false;
    }

    private String resolveTitle(Appointment appointment) {
        Doctor doctor = resolveDoctor(appointment);
        if (doctor == null) {
            return "预约详情";
        }
        Department department = dbHelper.getDepartmentDao().queryDepartmentById(doctor.getDepartmentId());
        String deptName = department != null ? department.getDeptName() : "未知科室";
        return deptName + " · " + doctor.getDoctorName();
    }

    private long resolveDoctorId(Appointment appointment) {
        Doctor doctor = resolveDoctor(appointment);
        return doctor != null ? doctor.getId() : -1;
    }

    private Doctor resolveDoctor(Appointment appointment) {
        RegisterSource source = dbHelper.getRegisterSourceDao().querySourceById(appointment.getSourceId());
        if (source == null) {
            return null;
        }
        DoctorSchedule schedule = dbHelper.getDoctorScheduleDao().queryScheduleById(source.getScheduleId());
        if (schedule == null) {
            return null;
        }
        return dbHelper.getDoctorDao().queryDoctorById(schedule.getDoctorId());
    }
}
