package com.serenehealth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;

import java.util.List;

/**
 * 首页快捷入口横向滚动适配器。
 * 当前首页未使用此组件，保留供后续版本在顶部区域添加横向滚动快捷入口。
 * TODO 2026-06-24 待需求明确后集成到 fragment_home.xml
 */
public class QuickEntryAdapter extends RecyclerView.Adapter<QuickEntryAdapter.ViewHolder> {

    private final Context context;
    private final List<String> entryList;
    private OnEntryClickListener clickListener;

    public interface OnEntryClickListener {
        void onEntryClick(int position, String title);
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.clickListener = listener;
    }

    public QuickEntryAdapter(Context context, List<String> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_quick_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = entryList.get(position);
        holder.textView.setText(title);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onEntryClick(holder.getAdapterPosition(), title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.quick_entry_text);
        }
    }
}
