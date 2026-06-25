package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Appointment;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.databinding.ItemAppointmentRecordBinding;
import com.serenehealth.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRecordAdapter extends RecyclerView.Adapter<AppointmentRecordAdapter.ViewHolder> {

    private final DBHelper dbHelper;
    private final OnAppointmentActionListener listener;
    private List<Appointment> appointments = new ArrayList<>();

    public interface OnAppointmentActionListener {
        void onCancel(Appointment appointment);

        void onFeedback(Appointment appointment);
    }

    public AppointmentRecordAdapter(DBHelper dbHelper, OnAppointmentActionListener listener) {
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void setData(List<Appointment> appointments) {
        this.appointments = appointments != null ? appointments : new ArrayList<Appointment>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentRecordBinding binding = ItemAppointmentRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(appointments.get(position));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemAppointmentRecordBinding binding;

        ViewHolder(ItemAppointmentRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Appointment appointment) {
            binding.tvTitle.setText(resolveTitle(appointment));
            binding.tvStatus.setText(mapStatus(appointment.getAppointmentStatus()));
            binding.tvTime.setText(resolveVisitTime(appointment));
            binding.tvNo.setText("预约编号：" + appointment.getAppointmentNo());

            String reason = appointment.getCancelReason();
            if (reason != null && !reason.isEmpty()) {
                binding.tvCancelReason.setVisibility(View.VISIBLE);
                binding.tvCancelReason.setText("取消原因：" + reason);
            } else {
                binding.tvCancelReason.setVisibility(View.GONE);
            }

            boolean booked = "BOOKED".equals(appointment.getAppointmentStatus());
            boolean visited = "VISITED".equals(appointment.getAppointmentStatus());
            binding.btnCancel.setVisibility(booked ? View.VISIBLE : View.GONE);
            binding.btnFeedback.setVisibility(visited ? View.VISIBLE : View.GONE);
            binding.btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel(appointment);
                }
            });
            binding.btnFeedback.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFeedback(appointment);
                }
            });
        }
    }

    private String resolveTitle(Appointment appointment) {
        RegisterSource source = dbHelper.getRegisterSourceDao().querySourceById(appointment.getSourceId());
        if (source == null) {
            return "预约详情";
        }
        DoctorSchedule schedule = dbHelper.getDoctorScheduleDao().queryScheduleById(source.getScheduleId());
        if (schedule == null) {
            return "预约详情";
        }
        Doctor doctor = dbHelper.getDoctorDao().queryDoctorById(schedule.getDoctorId());
        if (doctor == null) {
            return "预约详情";
        }
        Department department = dbHelper.getDepartmentDao().queryDepartmentById(doctor.getDepartmentId());
        String deptName = department != null ? department.getDeptName() : "未知科室";
        return deptName + " · " + doctor.getDoctorName();
    }

    private String resolveVisitTime(Appointment appointment) {
        RegisterSource source = dbHelper.getRegisterSourceDao().querySourceById(appointment.getSourceId());
        if (source == null) {
            return "创建时间：" + safeText(appointment.getCreateTime());
        }
        DoctorSchedule schedule = dbHelper.getDoctorScheduleDao().queryScheduleById(source.getScheduleId());
        if (schedule == null) {
            return "创建时间：" + safeText(appointment.getCreateTime());
        }
        return "就诊时间：" + safeText(schedule.getScheduleDate()) + " "
                + trimSecond(source.getSlotStartTime()) + "-" + trimSecond(source.getSlotEndTime());
    }

    private String mapStatus(String status) {
        if ("BOOKED".equals(status)) {
            return "已预约";
        } else if ("VISITED".equals(status)) {
            return "已就诊";
        } else if ("CANCELED".equals(status)) {
            return "已取消";
        } else if ("EXPIRED".equals(status)) {
            return "已过期";
        }
        return status != null ? status : "";
    }

    private String trimSecond(String time) {
        if (time != null && time.length() >= 5) {
            return time.substring(0, 5);
        }
        return safeText(time);
    }

    private String safeText(String text) {
        return text != null ? text : "";
    }
}
