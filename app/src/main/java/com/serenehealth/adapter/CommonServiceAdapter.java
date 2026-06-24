package com.serenehealth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;

import java.util.List;

/**
 * 首页常用服务网格适配器（4列×2行）
 * 每个项目显示圆形图标 + 下方文字
 */
public class CommonServiceAdapter extends RecyclerView.Adapter<CommonServiceAdapter.ViewHolder> {

    private final Context context;
    private final List<CommonServiceItem> itemList;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, CommonServiceItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public CommonServiceAdapter(Context context, List<CommonServiceItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_home_common_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommonServiceItem item = itemList.get(position);
        holder.iconImage.setImageResource(item.iconResId);
        holder.iconImage.setImageTintList(
                android.content.res.ColorStateList.valueOf(
                        context.getColor(R.color.primary)));
        holder.titleText.setText(item.title);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(holder.getAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconImage;
        final TextView titleText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iv_icon);
            titleText = itemView.findViewById(R.id.tv_title);
        }
    }

    /**
     * 常用服务数据模型
     */
    public static class CommonServiceItem {
        public final int iconResId;
        public final String title;

        public CommonServiceItem(int iconResId, String title) {
            this.iconResId = iconResId;
            this.title = title;
        }
    }
}
