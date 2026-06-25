package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.bean.Appointment;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.Feedback;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.databinding.ItemFeedbackAppointmentBinding;
import com.serenehealth.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class FeedbackAppointmentAdapter
        extends RecyclerView.Adapter<FeedbackAppointmentAdapter.ViewHolder> {

    private final DBHelper dbHelper;
    private final OnAppointmentSelectListener listener;
    private List<Appointment> appointments = new ArrayList<>();

    public interface OnAppointmentSelectListener {
        void onSelect(Appointment appointment);
    }

    public FeedbackAppointmentAdapter(DBHelper dbHelper, OnAppointmentSelectListener listener) {
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
        ItemFeedbackAppointmentBinding binding = ItemFeedbackAppointmentBinding.inflate(
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

        private final ItemFeedbackAppointmentBinding binding;

        ViewHolder(ItemFeedbackAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Appointment appointment) {
            Feedback feedback = dbHelper.getFeedbackDao().queryByAppointmentId(appointment.getId());
            binding.tvTitle.setText(resolveTitle(appointment));
            binding.tvTime.setText(resolveVisitTime(appointment));
            binding.tvNo.setText("预约编号：" + appointment.getAppointmentNo());
            binding.tvState.setText(feedback == null ? "未评价" : "已评价，可修改");
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSelect(appointment);
                }
            });
        }
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
