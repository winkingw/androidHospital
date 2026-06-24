package com.serenehealth.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Doctor;
import com.serenehealth.databinding.ItemDoctorBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> dataList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private Map<Long, Boolean> hasSlotMap = new HashMap<>();

    public interface OnItemClickListener {
        void onItemClick(Doctor doctor);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setData(List<Doctor> list) {
        this.dataList = list != null ? list : new ArrayList<Doctor>();
        notifyDataSetChanged();
    }

    public void setHasSlotMap(Map<Long, Boolean> map) {
        this.hasSlotMap = map != null ? map : new HashMap<Long, Boolean>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor item = dataList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDoctorBinding binding;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemDoctorBinding.bind(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position >= 0 && position < dataList.size()
                            && onItemClickListener != null) {
                        onItemClickListener.onItemClick(dataList.get(position));
                    }
                }
            });
        }

        void bind(Doctor doctor) {
            // 姓名
            String name = doctor.getDoctorName();
            binding.tvDoctorName.setText(name != null ? name : "");

            // 职称标签
            String title = doctor.getTitle();
            if (!TextUtils.isEmpty(title)) {
                binding.tvDoctorTitleBadge.setText(title);
                binding.tvDoctorTitleBadge.setVisibility(View.VISIBLE);
                // 主任医师用 primary 样式, 其他用灰色
                if (title.contains("主任")) {
                    binding.tvDoctorTitleBadge.setBackgroundResource(R.drawable.shape_badge_primary);
                    binding.tvDoctorTitleBadge.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.primary_container));
                } else {
                    binding.tvDoctorTitleBadge.setBackgroundResource(R.drawable.shape_badge_gray);
                    binding.tvDoctorTitleBadge.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.on_surface_variant));
                }
            } else {
                binding.tvDoctorTitleBadge.setVisibility(View.GONE);
            }

            // 简介（擅长领域）
            String introduction = doctor.getIntroduction();
            if (!TextUtils.isEmpty(introduction)) {
                binding.tvDoctorIntro.setText(introduction);
                binding.tvDoctorIntro.setVisibility(View.VISIBLE);
                binding.tvExpertiseLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvDoctorIntro.setVisibility(View.GONE);
                binding.tvExpertiseLabel.setVisibility(View.GONE);
            }

            // 科室名 — 不在列表卡片中显示具体科室（已在标题栏）
            binding.tvDoctorDept.setVisibility(View.GONE);

            // 号源状态
            Boolean hasSlot = hasSlotMap.get(doctor.getId());
            if (hasSlot != null) {
                binding.tvSlotStatus.setVisibility(View.VISIBLE);
                if (hasSlot) {
                    binding.tvSlotStatus.setText(R.string.doctor_has_slot);
                    binding.tvSlotStatus.setBackgroundResource(R.drawable.shape_tab_selected);
                    binding.tvSlotStatus.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.white));
                } else {
                    binding.tvSlotStatus.setText(R.string.doctor_no_slot);
                    binding.tvSlotStatus.setBackgroundResource(R.drawable.shape_tab_unselected);
                    binding.tvSlotStatus.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.on_surface_variant));
                }
            } else {
                binding.tvSlotStatus.setVisibility(View.GONE);
            }
        }
    }
}
