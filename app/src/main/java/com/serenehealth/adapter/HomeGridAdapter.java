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
 * 首页功能宫格适配器（4列）
 * 每个宫格显示图标（顶部）+ 文字（底部）
 */
public class HomeGridAdapter extends RecyclerView.Adapter<HomeGridAdapter.GridViewHolder> {

    private final Context context;
    private final List<GridItem> itemList;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, GridItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public HomeGridAdapter(Context context, List<GridItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_home_grid, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        GridItem item = itemList.get(position);
        holder.iconImage.setImageResource(item.iconResId);
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

    static class GridViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconImage;
        final TextView titleText;

        GridViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.grid_icon);
            titleText = itemView.findViewById(R.id.grid_title);
        }
    }

    /**
     * 功能宫格数据模型
     */
    public static class GridItem {
        public final int iconResId;
        public final String title;
        public final int actionType;

        public GridItem(int iconResId, String title, int actionType) {
            this.iconResId = iconResId;
            this.title = title;
            this.actionType = actionType;
        }
    }

    /** Action type constants */
    public static final int ACTION_DEPARTMENT = 1;
    public static final int ACTION_SMART_DIAGNOSIS = 2;
    public static final int ACTION_MESSAGE = 3;
    public static final int ACTION_NAVIGATION = 4;
    public static final int ACTION_LECTURE = 5;
    public static final int ACTION_PAYMENT = 6;
    public static final int ACTION_APPOINTMENT = 7;
    public static final int ACTION_PROFILE = 8;
}
