package com.serenehealth.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.serenehealth.R;
import com.serenehealth.activity.AppointmentRecordActivity;
import com.serenehealth.activity.DepartmentListActivity;
import com.serenehealth.activity.LoginActivity;
import com.serenehealth.activity.MessageCenterActivity;
import com.serenehealth.activity.PaymentActivity;
import com.serenehealth.activity.QRCodeVerificationActivity;
import com.serenehealth.activity.SmartDiagnosisActivity;
import com.serenehealth.adapter.BannerAdapter;
import com.serenehealth.bean.Banner;
import com.serenehealth.databinding.FragmentHomeBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.List;

/**
 * 首页 Fragment
 * 采用 Bento Grid 卡片式布局，包含轮播图、健康码卡片和服务入口。
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private OnHomeActionListener actionListener;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private List<Banner> bannerList;
    private boolean isBannerAutoScrollEvent;
    private final ViewPager2.OnPageChangeCallback bannerPageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (!isBannerAutoScrollEvent) {
                        startBannerAutoScroll();
                    }
                }
            };
    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null || bannerList == null || bannerList.size() <= 1) {
                return;
            }
            int nextItem = (binding.bannerViewPager.getCurrentItem() + 1) % bannerList.size();
            isBannerAutoScrollEvent = true;
            binding.bannerViewPager.setCurrentItem(nextItem, true);
            isBannerAutoScrollEvent = false;
            bannerHandler.postDelayed(this, 3000);
        }
    };

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

        DBHelper dbHelper = DBHelper.getInstance(context);
        setupBanners(dbHelper);

        // 加载未读消息数
        long userId = SPUtil.getCurrentUserId();
        if (userId > 0) {
            int unreadCount = dbHelper.getMessageDao().queryUnreadCount(userId);
            updateMessageBadge(unreadCount);
        }
    }

    // ==================== 轮播图 ====================

    private void setupBanners(DBHelper dbHelper) {
        bannerList = dbHelper.getBannerDao().queryActiveBanners();
        if (bannerList == null || bannerList.isEmpty()) {
            binding.bannerViewPager.setVisibility(View.GONE);
            return;
        }
        binding.bannerViewPager.setAdapter(new BannerAdapter(requireContext(), bannerList));
        binding.bannerViewPager.setOffscreenPageLimit(1);
        binding.bannerViewPager.setVisibility(View.VISIBLE);
        binding.bannerViewPager.unregisterOnPageChangeCallback(bannerPageChangeCallback);
        binding.bannerViewPager.registerOnPageChangeCallback(bannerPageChangeCallback);
        startBannerAutoScroll();
    }

    private void startBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
        if (bannerList != null && bannerList.size() > 1) {
            bannerHandler.postDelayed(bannerRunnable, 3000);
        }
    }

    private void stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
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
        binding.heroCard.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), QRCodeVerificationActivity.class));
        });

        // C1. 科室挂号 → DepartmentListActivity
        binding.cardDepartment.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), DepartmentListActivity.class)));

        // C2. 自助缴费 → 队员C 负责
        binding.cardPayment.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), PaymentActivity.class));
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
    public void onResume() {
        super.onResume();
        if (binding != null && getContext() != null) {
            setupBanners(DBHelper.getInstance(requireContext()));
        }
        startBannerAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopBannerAutoScroll();
        if (binding != null) {
            binding.bannerViewPager.unregisterOnPageChangeCallback(bannerPageChangeCallback);
        }
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }
}
