package com.serenehealth.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.serenehealth.R;
import com.serenehealth.activity.AppointmentRecordActivity;
import com.serenehealth.activity.DepartmentListActivity;
import com.serenehealth.activity.LoginActivity;
import com.serenehealth.activity.MessageCenterActivity;
import com.serenehealth.activity.SmartDiagnosisActivity;
import com.serenehealth.adapter.CommonServiceAdapter;
import com.serenehealth.databinding.FragmentHomeBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 首页 Fragment
 * 采用 Bento Grid 卡片式布局，包含健康码卡片、快速服务、常用服务等功能模块。
 * 移除旧版轮播图逻辑，使用静态健康码卡片替代。
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CommonServiceAdapter commonServiceAdapter;
    private OnHomeActionListener actionListener;

    /**
     * 首页操作回调，由宿主 Activity 实现
     */
    public interface OnHomeActionListener {
        /** 切换到个人中心 Fragment */
        void switchToProfile();
        /** 切换到健康档案 Fragment */
        void switchToHealthArchive();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeActionListener) {
            actionListener = (OnHomeActionListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        setListeners();
    }

    // ==================== 数据初始化 ====================

    private void initData() {
        Context context = getContext();
        if (context == null) return;

        // 加载常用服务网格
        setupCommonServices(context);

        // 加载未读消息数
        DBHelper dbHelper = DBHelper.getInstance(context);
        long userId = SPUtil.getCurrentUserId();
        if (userId > 0) {
            int unreadCount = dbHelper.getMessageDao().queryUnreadCount(userId);
            updateMessageBadge(unreadCount);
        }
    }

    // ==================== 常用服务网格 ====================

    private void setupCommonServices(Context context) {
        List<CommonServiceAdapter.CommonServiceItem> items = Arrays.asList(
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_appointment,
                        getString(R.string.home_check_appointment)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_lecture,
                        getString(R.string.home_report_query)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_message,
                        getString(R.string.home_online_consult)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_navigation,
                        getString(R.string.home_dept_navigation)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_payment,
                        getString(R.string.home_medicine_query)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_appointment,
                        getString(R.string.home_physical_exam_book)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_ai_guide,
                        getString(R.string.home_visit_guide)),
                new CommonServiceAdapter.CommonServiceItem(
                        R.drawable.ic_home_profile,
                        getString(R.string.home_health_assessment))
        );

        binding.commonServiceRecycler.setLayoutManager(
                new GridLayoutManager(context, 4));
        commonServiceAdapter = new CommonServiceAdapter(context, items);
        binding.commonServiceRecycler.setAdapter(commonServiceAdapter);
    }

    // ==================== 角标 ====================

    private void updateMessageBadge(int count) {
        if (count > 0) {
            binding.badgeUnread.setVisibility(View.VISIBLE);
            binding.badgeUnread.setText(String.valueOf(Math.min(count, 99)));
        } else {
            binding.badgeUnread.setVisibility(View.GONE);
        }
    }

    // ==================== 事件监听 ====================

    private void setListeners() {
        Context context = getContext();
        if (context == null) return;

        // B. 健康码卡片
        binding.heroCard.setOnClickListener(v ->
                Toast.makeText(context, R.string.home_developing, Toast.LENGTH_SHORT).show());

        // C1. 科室挂号 → DepartmentListActivity
        binding.cardDepartment.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), DepartmentListActivity.class)));

        // C2. 自助缴费 → 队员C 负责
        binding.cardPayment.setOnClickListener(v -> {
            if (!checkLogin()) return;
            Toast.makeText(context, R.string.home_developing, Toast.LENGTH_SHORT).show();
        });

        // D1. 预约记录
        binding.cardAppointment.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), AppointmentRecordActivity.class));
        });

        // D2. 智能导航
        binding.cardNavigation.setOnClickListener(v -> openNavigation(context));

        // D3. 消息中心
        binding.cardMessage.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), MessageCenterActivity.class));
        });

        // D4. 智能导诊
        binding.cardDiagnosis.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), SmartDiagnosisActivity.class));
        });

        // E. 健康讲堂
        binding.lectureBanner.setOnClickListener(v -> openLecture(context));

        // 消息图标
        binding.messageIconContainer.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), MessageCenterActivity.class));
        });

        // 常用服务点击
        commonServiceAdapter.setOnItemClickListener((position, item) -> {
            String title = item.title;
            if (title.equals(getString(R.string.home_check_appointment))) {
                startActivity(new Intent(requireActivity(), DepartmentListActivity.class));
            } else if (title.equals(getString(R.string.home_report_query))) {
                if (actionListener != null) {
                    actionListener.switchToHealthArchive();
                }
            } else if (title.equals(getString(R.string.home_dept_navigation))) {
                openNavigation(context);
            } else if (title.equals(getString(R.string.home_health_assessment))) {
                if (actionListener != null) {
                    actionListener.switchToHealthArchive();
                }
            } else {
                Toast.makeText(context, title + " - " + getString(R.string.home_developing),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 检查登录状态，未登录则跳转到登录页
     */
    private boolean checkLogin() {
        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return false;
        }
        return true;
    }

    // ==================== 导航逻辑 ====================

    /**
     * 智能导航 - 调用地图应用
     */
    private void openNavigation(Context context) {
        try {
            String query = getString(R.string.map_search_query);
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.home_no_map_app, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 健康讲堂 - 打开浏览器
     */
    private void openLecture(Context context) {
        try {
            Uri uri = Uri.parse(getString(R.string.url_health_lecture));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.home_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== 生命周期 ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }
}
