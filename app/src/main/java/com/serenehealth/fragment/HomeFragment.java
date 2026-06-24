package com.serenehealth.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.activity.DepartmentListActivity;
import com.serenehealth.activity.MessageCenterActivity;
import com.serenehealth.activity.SmartDiagnosisActivity;
import com.serenehealth.adapter.BannerAdapter;
import com.serenehealth.adapter.HomeGridAdapter;
import com.serenehealth.adapter.QuickEntryAdapter;
import com.serenehealth.bean.Banner;
import com.serenehealth.databinding.FragmentHomeBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final long BANNER_INTERVAL_MS = 3000L;

    private FragmentHomeBinding binding;
    private BannerAdapter bannerAdapter;
    private HomeGridAdapter gridAdapter;
    private QuickEntryAdapter quickEntryAdapter;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int bannerCurrentPosition = 0;

    private OnHomeActionListener actionListener;

    /**
     * 首页操作回调，由宿主 Activity 实现
     */
    public interface OnHomeActionListener {
        /** 切换到个人中心 Fragment */
        void switchToProfile();
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

        // 1. 加载轮播图
        List<Banner> banners = dbHelper.getBannerDao().queryActiveBanners();
        setupBanner(context, banners);

        // 2. 设置功能宫格
        setupGrid(context);

        // 3. 设置快捷入口
        setupQuickEntries(context);

        // 4. 加载未读消息数
        long userId = SPUtil.getCurrentUserId();
        if (userId > 0) {
            int unreadCount = dbHelper.getMessageDao().queryUnreadCount(userId);
            updateMessageBadge(unreadCount);
        }
    }

    // ==================== 轮播图 ====================

    private void setupBanner(Context context, List<Banner> banners) {
        if (banners == null || banners.isEmpty()) {
            binding.bannerContainer.setVisibility(View.GONE);
            return;
        }

        binding.bannerContainer.setVisibility(View.VISIBLE);

        // RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false);
        binding.bannerRecycler.setLayoutManager(layoutManager);

        bannerAdapter = new BannerAdapter(context, banners);
        binding.bannerRecycler.setAdapter(bannerAdapter);

        // PagerSnapHelper 实现一页一滑
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.bannerRecycler);

        // 指示器
        setupIndicatorDots(banners.size());

        // 手动滑动暂停自动轮播
        binding.bannerRecycler.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                stopBannerAutoScroll();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                startBannerAutoScroll();
            }
            return false;
        });

        // 监听滑动结束，更新指示器
        binding.bannerRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        int pos = lm.findFirstVisibleItemPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            updateIndicator(pos);
                            bannerCurrentPosition = pos;
                        }
                    }
                }
            }
        });

        // 多于 1 页才自动轮播
        if (banners.size() > 1) {
            startBannerAutoScroll();
        }
    }

    private void setupIndicatorDots(int count) {
        binding.bannerIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_indicator));
            dot.setAlpha(i == 0 ? 1.0f : 0.35f);
            binding.bannerIndicator.addView(dot);
        }
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < binding.bannerIndicator.getChildCount(); i++) {
            View dot = binding.bannerIndicator.getChildAt(i);
            dot.setAlpha(i == position ? 1.0f : 0.35f);
        }
    }

    private void startBannerAutoScroll() {
        stopBannerAutoScroll();
        if (bannerAdapter == null || bannerAdapter.getItemCount() <= 1) return;

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int total = bannerAdapter.getItemCount();
                int nextPos = (bannerCurrentPosition + 1) % total;
                binding.bannerRecycler.smoothScrollToPosition(nextPos);
                bannerCurrentPosition = nextPos;
                bannerHandler.postDelayed(this, BANNER_INTERVAL_MS);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
            bannerRunnable = null;
        }
    }

    // ==================== 功能宫格 ====================

    private void setupGrid(Context context) {
        List<HomeGridAdapter.GridItem> items = Arrays.asList(
                new HomeGridAdapter.GridItem(R.drawable.ic_home_department,
                        getString(R.string.home_department),
                        HomeGridAdapter.ACTION_DEPARTMENT),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_ai_guide,
                        getString(R.string.home_ai_guide),
                        HomeGridAdapter.ACTION_SMART_DIAGNOSIS),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_message,
                        getString(R.string.home_message),
                        HomeGridAdapter.ACTION_MESSAGE),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_navigation,
                        getString(R.string.home_navigation),
                        HomeGridAdapter.ACTION_NAVIGATION),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_lecture,
                        getString(R.string.home_lecture),
                        HomeGridAdapter.ACTION_LECTURE),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_payment,
                        getString(R.string.home_payment),
                        HomeGridAdapter.ACTION_PAYMENT),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_appointment,
                        getString(R.string.home_appointment),
                        HomeGridAdapter.ACTION_APPOINTMENT),
                new HomeGridAdapter.GridItem(R.drawable.ic_home_profile,
                        getString(R.string.home_profile),
                        HomeGridAdapter.ACTION_PROFILE)
        );

        binding.gridRecycler.setLayoutManager(new GridLayoutManager(context, 4));
        gridAdapter = new HomeGridAdapter(context, items);
        binding.gridRecycler.setAdapter(gridAdapter);
    }

    // ==================== 快捷入口 ====================

    private void setupQuickEntries(Context context) {
        List<String> entries = Arrays.asList(
                getString(R.string.home_fever_clinic),
                getString(R.string.home_nucleic_acid),
                getString(R.string.home_physical_exam)
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false);
        binding.quickEntryRecycler.setLayoutManager(layoutManager);

        quickEntryAdapter = new QuickEntryAdapter(context, entries);
        binding.quickEntryRecycler.setAdapter(quickEntryAdapter);
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

        // 功能宫格点击
        gridAdapter.setOnItemClickListener((position, item) -> handleGridAction(item.actionType));

        // 消息图标
        binding.messageIconContainer.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), MessageCenterActivity.class));
        });

        // 搜索框
        binding.searchCard.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), DepartmentListActivity.class));
        });

        // 快捷入口
        quickEntryAdapter.setOnEntryClickListener((position, title) -> {
            Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show();
        });
    }

    // ==================== 导航逻辑 ====================

    private void handleGridAction(int actionType) {
        Context context = getContext();
        if (context == null) return;

        switch (actionType) {
            case HomeGridAdapter.ACTION_DEPARTMENT:
                startActivity(new Intent(context, DepartmentListActivity.class));
                break;

            case HomeGridAdapter.ACTION_SMART_DIAGNOSIS:
                startActivity(new Intent(context, SmartDiagnosisActivity.class));
                break;

            case HomeGridAdapter.ACTION_MESSAGE:
                startActivity(new Intent(context, MessageCenterActivity.class));
                break;

            case HomeGridAdapter.ACTION_NAVIGATION:
                openNavigation(context);
                break;

            case HomeGridAdapter.ACTION_LECTURE:
                openLecture(context);
                break;

            case HomeGridAdapter.ACTION_PAYMENT:
                Toast.makeText(context, R.string.home_developing, Toast.LENGTH_SHORT).show();
                break;

            case HomeGridAdapter.ACTION_APPOINTMENT:
                Toast.makeText(context, R.string.home_developing, Toast.LENGTH_SHORT).show();
                break;

            case HomeGridAdapter.ACTION_PROFILE:
                if (actionListener != null) {
                    actionListener.switchToProfile();
                }
                break;
        }
    }

    /**
     * 功能8：智能导航 - 调用地图应用
     */
    private void openNavigation(Context context) {
        try {
            Uri uri = Uri.parse("geo:0,0?q=医院");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.home_no_map_app, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 功能9：健康讲堂 - 打开浏览器
     */
    private void openLecture(Context context) {
        try {
            Uri uri = Uri.parse("https://www.haodf.com/");
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
        stopBannerAutoScroll();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }
}
