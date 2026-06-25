package com.serenehealth.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Department;
import com.serenehealth.databinding.ItemDepartmentBinding;

import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private List<Department> dataList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Department department);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setData(List<Department> list) {
        this.dataList = list != null ? list : new ArrayList<Department>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_department, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Department item = dataList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDepartmentBinding binding;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemDepartmentBinding.bind(itemView);

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

        void bind(Department department) {
            binding.tvDeptName.setText(department.getDeptName() != null
                    ? department.getDeptName() : "");

            String description = department.getDescription();
            if (!TextUtils.isEmpty(description)) {
                binding.tvDeptDescription.setText(description);
                binding.tvDeptDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvDeptDescription.setVisibility(View.GONE);
            }
        }
    }
}
