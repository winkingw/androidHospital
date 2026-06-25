package com.serenehealth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Banner;

import java.util.List;

/**
 * 首页轮播图适配器（纯色背景 + 标题文字展示）。
 */
public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final Context context;
    private final List<Banner> bannerList;

    private static final int[] BANNER_COLORS = {
            R.color.banner_blue_1,
            R.color.banner_blue_2,
            R.color.banner_blue_3,
            R.color.banner_blue_4,
            R.color.banner_blue_5
    };

    public BannerAdapter(Context context, List<Banner> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        holder.titleText.setText(banner.getTitle() != null ? banner.getTitle() : "");

        int colorIndex = position % BANNER_COLORS.length;
        int colorRes = BANNER_COLORS[colorIndex];
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, colorRes));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.banner_title);
        }
    }
}
