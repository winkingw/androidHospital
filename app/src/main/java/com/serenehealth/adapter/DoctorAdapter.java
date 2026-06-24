package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.bean.Doctor;
import com.serenehealth.databinding.ItemDoctorListBinding;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorClick(long doctorId);
    }

    private final List<Doctor> doctorList;
    private final OnDoctorClickListener listener;

    public DoctorAdapter(List<Doctor> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDoctorListBinding binding = ItemDoctorListBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDoctorListBinding binding;

        ViewHolder(ItemDoctorListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Doctor doctor) {
            binding.tvDoctorName.setText(doctor.getDoctorName());
            binding.tvTitle.setText(doctor.getTitle());
            binding.tvIntroduction.setText(doctor.getIntroduction());
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDoctorClick(doctor.getId());
                }
            });
        }
    }
}
