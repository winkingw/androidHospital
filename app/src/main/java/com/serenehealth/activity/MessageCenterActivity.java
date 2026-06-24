package com.serenehealth.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.adapter.MessageAdapter;
import com.serenehealth.bean.Message;
import com.serenehealth.databinding.ActivityMessageCenterBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.db.MessageDao;
import com.serenehealth.util.SPUtil;

import java.util.List;

public class MessageCenterActivity extends AppCompatActivity {

    private ActivityMessageCenterBinding binding;
    private MessageDao messageDao;
    private MessageAdapter adapter;
    private long currentUserId;

    // Tab views
    private TextView tabAll;
    private TextView tabSystem;
    private TextView tabAppointment;
    private TextView tabOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMessageCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageDao = DBHelper.getInstance(this).getMessageDao();
        currentUserId = SPUtil.getCurrentUserId();

        initViews();
        initTabs();
        loadMessages();
    }

    private void initViews() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MessageAdapter(messageDao, new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message message) {
                showMessageDetail(message);
            }
        });
        recyclerView.setAdapter(adapter);

        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 全部已读
        binding.btnMarkAllRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDao.markAllAsRead(currentUserId);
                loadMessages();
            }
        });

        // Tab views
        tabAll = binding.tabAll;
        tabSystem = binding.tabSystem;
        tabAppointment = binding.tabAppointment;
        tabOther = binding.tabOther;
    }

    private void initTabs() {
        tabAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabAll);
                adapter.setFilter("all");
                updateEmptyView();
            }
        });

        tabSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabSystem);
                adapter.setFilter("system");
                updateEmptyView();
            }
        });

        tabAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabAppointment);
                adapter.setFilter("appointment");
                updateEmptyView();
            }
        });

        tabOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabOther);
                adapter.setFilter("other");
                updateEmptyView();
            }
        });
    }

    private void selectTab(TextView selectedTab) {
        TextView[] tabs = new TextView[]{tabAll, tabSystem, tabAppointment, tabOther};
        for (TextView tab : tabs) {
            tab.setBackgroundResource(tab == selectedTab
                    ? R.drawable.shape_tab_selected : R.drawable.shape_tab_unselected);
            tab.setTextColor(tab == selectedTab
                    ? getColor(R.color.white) : getColor(R.color.on_surface_variant));
        }
    }

    private void loadMessages() {
        List<Message> messageList = messageDao.queryByUserId(currentUserId);
        adapter.setData(messageList);
        updateEmptyView();
    }

    private void updateEmptyView() {
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showMessageDetail(Message message) {
        new AlertDialog.Builder(this)
                .setTitle(message.getTitle() != null ? message.getTitle() : "")
                .setMessage(message.getContent() != null ? message.getContent() : "")
                .setPositiveButton(R.string.message_center_dialog_confirm, null)
                .show();
    }
}
