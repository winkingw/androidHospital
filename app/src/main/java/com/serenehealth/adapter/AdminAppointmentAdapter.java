package com.serenehealth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.DoctorAppointmentDTO;
import com.serenehealth.databinding.ItemAdminAppointmentBinding;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
    private final List<DoctorAppointmentDTO> appointmentList;

    public AdminAppointmentAdapter(List<DoctorAppointmentDTO> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAppointmentBinding binding = ItemAdminAppointmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        DoctorAppointmentDTO appt = appointmentList.get(position);

        holder.binding.tvPatientName.setText(appt.getPatientName());

        String status = appt.getAppointmentStatus();
        holder.binding.tvStatus.setText(mapStatus(status));
        holder.binding.tvStatus.setTextColor(getStatusColor(context, status));

        String periodText = "MORNING".equals(appt.getPeriod()) ? "上午" :
                "AFTERNOON".equals(appt.getPeriod()) ? "下午" : appt.getPeriod();

        String slotTime = appt.getSlotStartTime() != null && appt.getSlotEndTime() != null
                ? appt.getSlotStartTime().substring(0, 5) + "-" + appt.getSlotEndTime().substring(0, 5)
                : "";

        String scheduleInfo = appt.getScheduleDate() + "  " + periodText + "  " + slotTime;
        holder.binding.tvScheduleInfo.setText(scheduleInfo);

        String clinicRoom = appt.getClinicRoom() != null ? appt.getClinicRoom() : "未指定";
        String deptClinic = context.getString(R.string.doctor_appt_dept_clinic_format,
                appt.getDepartmentName(), clinicRoom);
        holder.binding.tvDeptClinic.setText(deptClinic);

        holder.binding.tvAppointmentNo.setText(
                context.getString(R.string.appointment_no_prefix) + appt.getAppointmentNo());
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    private String mapStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "BOOKED":
                return "已预约";
            case "VISITED":
                return "已就诊";
            case "CANCELED":
                return "已取消";
            case "EXPIRED":
                return "已过期";
            default:
                return status;
        }
    }

    private int getStatusColor(Context context, String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.on_surface);
        switch (status) {
            case "BOOKED":
                return ContextCompat.getColor(context, R.color.primary);
            case "VISITED":
                return ContextCompat.getColor(context, R.color.status_visited);
            case "CANCELED":
                return ContextCompat.getColor(context, R.color.error);
            case "EXPIRED":
                return ContextCompat.getColor(context, R.color.on_surface_variant);
            default:
                return ContextCompat.getColor(context, R.color.on_surface);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAdminAppointmentBinding binding;

        ViewHolder(ItemAdminAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
