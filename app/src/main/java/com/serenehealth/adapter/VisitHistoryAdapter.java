package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.bean.VisitHistory;
import com.serenehealth.databinding.ItemVisitHistoryBinding;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VisitHistoryAdapter extends RecyclerView.Adapter<VisitHistoryAdapter.ViewHolder> {

    private final List<VisitHistory> visitList;
    private final Map<Long, String> departmentMap;
    private final Map<Long, String> doctorMap;

    public VisitHistoryAdapter(List<VisitHistory> visitList,
                               Map<Long, String> departmentMap,
                               Map<Long, String> doctorMap) {
        this.visitList = visitList;
        this.departmentMap = departmentMap;
        this.doctorMap = doctorMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVisitHistoryBinding binding = ItemVisitHistoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitHistory item = visitList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemVisitHistoryBinding binding;

        ViewHolder(ItemVisitHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VisitHistory item) {
            String deptName = departmentMap.getOrDefault(item.getDepartmentId(), "未知科室");
            String doctorName = doctorMap.getOrDefault(item.getDoctorId(), "未知医生");
            binding.tvVisitTime.setText(item.getVisitTime());
            binding.tvDeptDoctor.setText(String.format(Locale.getDefault(), "%s · %s", deptName, doctorName));
            binding.tvChiefComplaint.setText(item.getChiefComplaint());
            binding.tvDiagnosis.setText(item.getDiagnosis());
            binding.tvTreatmentAdvice.setText(item.getTreatmentAdvice());
        }
    }
}
