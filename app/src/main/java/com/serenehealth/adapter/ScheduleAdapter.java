package com.serenehealth.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.RegisterSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 排班列表适配器
 * 展示排班时段及该时段下的号源槽位（3列网格）
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<DoctorSchedule> scheduleList = new ArrayList<>();
    private Map<Long, List<RegisterSource>> sourceMap;
    private OnSlotClickListener slotClickListener;

    public interface OnSlotClickListener {
        void onSlotClick(DoctorSchedule schedule, RegisterSource source);
    }

    public ScheduleAdapter(OnSlotClickListener listener) {
        this.slotClickListener = listener;
    }

    public void setData(List<DoctorSchedule> schedules, Map<Long, List<RegisterSource>> sources) {
        this.scheduleList = schedules != null ? schedules : new ArrayList<DoctorSchedule>();
        this.sourceMap = sources;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorSchedule schedule = scheduleList.get(position);
        Context context = holder.itemView.getContext();

        // 时段标签
        holder.tvPeriodLabel.setText(schedule.getPeriod() != null ? schedule.getPeriod() : "");

        // 时间范围
        String timeRange = (schedule.getStartTime() != null ? schedule.getStartTime() : "")
                + "-" + (schedule.getEndTime() != null ? schedule.getEndTime() : "");
        holder.tvTimeRange.setText(timeRange);

        // 诊室位置
        if (schedule.getClinicRoom() != null && !schedule.getClinicRoom().isEmpty()) {
            holder.tvClinicRoom.setText(context.getString(R.string.doctor_detail_clinic_room,
                    schedule.getClinicRoom()));
            holder.tvClinicRoom.setVisibility(View.VISIBLE);
        } else {
            holder.tvClinicRoom.setVisibility(View.GONE);
        }

        // 动态构建3列网格槽位
        holder.llSlotContainer.removeAllViews();
        List<RegisterSource> sources = null;
        if (sourceMap != null) {
            sources = sourceMap.get(schedule.getId());
        }

        if (sources != null && !sources.isEmpty()) {
            LinearLayout currentRow = null;
            for (int i = 0; i < sources.size(); i++) {
                // 每3个槽位新建一行
                if (i % 3 == 0) {
                    currentRow = new LinearLayout(context);
                    currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    holder.llSlotContainer.addView(currentRow);
                }

                final RegisterSource source = sources.get(i);
                View slotChip = createSlotChip(context, schedule, source);
                if (currentRow != null) {
                    currentRow.addView(slotChip);
                }
            }
        }
    }

    /**
     * 创建单个槽位 Chip View
     */
    private View createSlotChip(Context context, final DoctorSchedule schedule,
                                 final RegisterSource source) {
        LinearLayout chip = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        int margin = (int) context.getResources().getDimension(R.dimen.slot_chip_margin);
        lp.setMargins(margin / 2, margin / 2, margin / 2, margin / 2);
        chip.setLayoutParams(lp);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setPadding(
                (int) context.getResources().getDimension(R.dimen.spacing_small),
                (int) context.getResources().getDimension(R.dimen.spacing_tiny),
                (int) context.getResources().getDimension(R.dimen.spacing_small),
                (int) context.getResources().getDimension(R.dimen.spacing_tiny));
        chip.setMinimumHeight((int) context.getResources().getDimension(R.dimen.slot_chip_height));

        // 时间文字
        TextView tvTime = new TextView(context);
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_caption));
        tvTime.setText(source.getSlotStartTime() + "-" + source.getSlotEndTime());

        // 状态文字
        TextView tvStatus = new TextView(context);
        tvStatus.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_tiny));

        if (source.getRemainNum() > 0) {
            // 有余号：蓝色边框
            chip.setBackgroundResource(R.drawable.shape_slot_available);
            tvTime.setTextColor(context.getColor(R.color.on_surface));
            tvStatus.setText(String.format(Locale.getDefault(), "¥%.0f %s",
                    source.getRegisterFee(),
                    context.getString(R.string.doctor_detail_slot_remain,
                            source.getRemainNum())));
            tvStatus.setTextColor(context.getColor(R.color.primary_container));

            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (slotClickListener != null) {
                        slotClickListener.onSlotClick(schedule, source);
                    }
                }
            });
        } else {
            // 已约满：灰色背景
            chip.setBackgroundResource(R.drawable.shape_slot_full);
            tvTime.setTextColor(context.getColor(R.color.on_surface_variant));
            tvStatus.setText(context.getString(R.string.doctor_detail_full));
            tvStatus.setTextColor(context.getColor(R.color.on_surface_variant));
            chip.setClickable(false);
            chip.setEnabled(false);
        }

        chip.addView(tvTime);
        chip.addView(tvStatus);

        return chip;
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriodLabel;
        TextView tvTimeRange;
        TextView tvClinicRoom;
        LinearLayout llSlotContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriodLabel = itemView.findViewById(R.id.tv_period_label);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            tvClinicRoom = itemView.findViewById(R.id.tv_clinic_room);
            llSlotContainer = itemView.findViewById(R.id.ll_slot_container);
        }
    }
}
