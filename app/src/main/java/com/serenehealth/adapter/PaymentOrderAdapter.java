package com.serenehealth.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.bean.PaymentOrder;
import com.serenehealth.databinding.ItemPaymentOrderBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentOrderAdapter extends RecyclerView.Adapter<PaymentOrderAdapter.ViewHolder> {

    private final OnPaymentActionListener listener;
    private List<PaymentOrder> orders = new ArrayList<>();

    public interface OnPaymentActionListener {
        void onMockPay(PaymentOrder order);

        void onMedicalCardPay(PaymentOrder order);
    }

    public PaymentOrderAdapter(OnPaymentActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<PaymentOrder> orders) {
        this.orders = orders != null ? orders : new ArrayList<PaymentOrder>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentOrderBinding binding = ItemPaymentOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPaymentOrderBinding binding;

        ViewHolder(ItemPaymentOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PaymentOrder order) {
            binding.tvOrderNo.setText(order.getOrderNo());
            binding.tvStatus.setText(mapStatus(order.getOrderStatus()));
            binding.tvAppointment.setText(resolveAppointmentInfo(order));
            binding.tvAmount.setText(String.format(Locale.getDefault(), "¥%.2f", order.getAmount()));
            binding.tvPayInfo.setText(resolvePayInfo(order));

            boolean unpaid = "UNPAID".equals(order.getOrderStatus());
            binding.layoutActions.setVisibility(unpaid ? View.VISIBLE : View.GONE);
            binding.btnMockPay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMockPay(order);
                }
            });
            binding.btnMedicalCardPay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicalCardPay(order);
                }
            });
        }
    }

    private String resolveAppointmentInfo(PaymentOrder order) {
        StringBuilder builder = new StringBuilder();
        if (hasText(order.getAppointmentNo())) {
            builder.append("预约编号：").append(order.getAppointmentNo());
        } else {
            builder.append("关联预约ID：").append(order.getAppointmentId());
        }

        String deptDoctor = joinNonEmpty(order.getDepartmentName(), order.getDoctorName(), "  ");
        if (hasText(deptDoctor)) {
            builder.append("\n").append(deptDoctor);
        }

        String periodText = mapPeriod(order.getPeriod());
        String slotTime = formatSlotTime(order.getSlotStartTime(), order.getSlotEndTime());
        String dateTime = joinNonEmpty(order.getScheduleDate(), periodText, "  ");
        dateTime = joinNonEmpty(dateTime, slotTime, "  ");
        if (hasText(dateTime)) {
            builder.append("\n").append(dateTime);
        }

        if (hasText(order.getClinicRoom())) {
            builder.append("\n诊室位置：").append(order.getClinicRoom());
        }
        return builder.toString();
    }

    private String resolvePayInfo(PaymentOrder order) {
        if (order.getPayTime() != null && !order.getPayTime().isEmpty()) {
            return "支付方式：" + order.getPayChannel() + "，支付时间：" + order.getPayTime();
        }
        return "支付方式：" + (order.getPayChannel() != null ? order.getPayChannel() : "MOCK_PAY");
    }

    private String mapPeriod(String period) {
        if ("MORNING".equals(period)) {
            return "上午";
        }
        if ("AFTERNOON".equals(period)) {
            return "下午";
        }
        return period != null ? period : "";
    }

    private String formatSlotTime(String startTime, String endTime) {
        if (!hasText(startTime) || !hasText(endTime)) {
            return "";
        }
        return trimTime(startTime) + "-" + trimTime(endTime);
    }

    private String trimTime(String time) {
        return time != null && time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private String joinNonEmpty(String first, String second, String separator) {
        if (hasText(first) && hasText(second)) {
            return first + separator + second;
        }
        if (hasText(first)) {
            return first;
        }
        return hasText(second) ? second : "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String mapStatus(String status) {
        if ("UNPAID".equals(status)) {
            return "待支付";
        } else if ("PAID".equals(status)) {
            return "已支付";
        } else if ("CANCELED".equals(status)) {
            return "已取消";
        } else if ("REFUNDED".equals(status)) {
            return "已退款";
        }
        return status != null ? status : "";
    }
}
