package com.serenehealth.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Message;
import com.serenehealth.db.MessageDao;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> allMessages = new ArrayList<>();
    private List<Message> displayMessages = new ArrayList<>();
    private String currentFilter = "all";
    private MessageDao messageDao;
    private OnMessageClickListener onMessageClickListener;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(MessageDao messageDao, OnMessageClickListener listener) {
        this.messageDao = messageDao;
        this.onMessageClickListener = listener;
    }

    public void setData(List<Message> messages) {
        this.allMessages = messages != null ? messages : new ArrayList<Message>();
        applyFilter(currentFilter);
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilter(filter);
    }

    private void applyFilter(String filter) {
        displayMessages.clear();
        if ("all".equals(filter)) {
            displayMessages.addAll(allMessages);
        } else if ("other".equals(filter)) {
            for (Message msg : allMessages) {
                String type = msg.getBusinessType();
                if (type == null || type.isEmpty()
                        || (!"system".equals(type) && !"appointment".equals(type))) {
                    displayMessages.add(msg);
                }
            }
        } else {
            for (Message msg : allMessages) {
                String type = msg.getBusinessType();
                if (type == null) {
                    type = "";
                }
                if (filter.equals(type)) {
                    displayMessages.add(msg);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = displayMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return displayMessages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private View unreadDot;
        private TextView tvTitle;
        private TextView tvSummary;
        private TextView tvTime;
        private View divider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            unreadDot = itemView.findViewById(R.id.unread_dot);
            tvTitle = itemView.findViewById(R.id.message_title);
            tvSummary = itemView.findViewById(R.id.message_summary);
            tvTime = itemView.findViewById(R.id.message_time);
            divider = itemView.findViewById(R.id.divider);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position >= 0 && position < displayMessages.size()) {
                        Message message = displayMessages.get(position);
                        if (message.getIsRead() == 0) {
                            messageDao.markAsRead(message.getId());
                            message.setIsRead(1);
                            notifyItemChanged(position);
                        }
                        if (onMessageClickListener != null) {
                            onMessageClickListener.onMessageClick(message);
                        }
                    }
                }
            });
        }

        void bind(Message message) {
            // 未读状态：显示蓝色圆点，标题加粗
            boolean isUnread = message.getIsRead() == 0;
            unreadDot.setVisibility(isUnread ? View.VISIBLE : View.GONE);
            tvTitle.getPaint().setFakeBoldText(isUnread);
            tvTitle.setTextColor(itemView.getContext().getColor(
                    isUnread ? R.color.on_surface : R.color.on_surface_variant));
            tvTitle.setText(message.getTitle() != null ? message.getTitle() : "");

            // 内容摘要：取前30字
            String content = message.getContent();
            if (!TextUtils.isEmpty(content)) {
                if (content.length() > 30) {
                    tvSummary.setText(content.substring(0, 30) + "...");
                } else {
                    tvSummary.setText(content);
                }
            } else {
                tvSummary.setText("");
            }

            // 时间
            String time = message.getSendTime();
            if (!TextUtils.isEmpty(time)) {
                // 只显示日期部分（前10个字符 yyyy-MM-dd）
                if (time.length() >= 10) {
                    tvTime.setText(time.substring(0, 10));
                } else {
                    tvTime.setText(time);
                }
            } else {
                tvTime.setText("");
            }

            // 最后一条不显示分割线
            boolean isLast = getAdapterPosition() == displayMessages.size() - 1;
            divider.setVisibility(isLast ? View.GONE : View.VISIBLE);
        }
    }
}
