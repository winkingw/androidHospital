package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.bean.MedicalCardRecord;
import com.serenehealth.databinding.ItemMedicalCardRecordBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicalCardRecordAdapter extends RecyclerView.Adapter<MedicalCardRecordAdapter.ViewHolder> {

    private List<MedicalCardRecord> records = new ArrayList<>();

    public void setData(List<MedicalCardRecord> records) {
        this.records = records != null ? records : new ArrayList<MedicalCardRecord>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMedicalCardRecordBinding binding = ItemMedicalCardRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMedicalCardRecordBinding binding;

        ViewHolder(ItemMedicalCardRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MedicalCardRecord record) {
            binding.tvDesc.setText(record.getDescription() != null ? record.getDescription() : record.getRecordType());
            binding.tvAmount.setText(String.format(Locale.getDefault(), "-¥%.2f", record.getAmount()));
            binding.tvTime.setText(record.getRecordTime());
            binding.tvBalance.setText(String.format(Locale.getDefault(), "消费后余额：¥%.2f",
                    record.getBalanceAfter()));
        }
    }
}
