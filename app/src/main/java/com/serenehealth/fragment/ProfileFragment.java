package com.serenehealth.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.serenehealth.R;
import com.serenehealth.activity.AppointmentRecordActivity;
import com.serenehealth.activity.FeedbackActivity;
import com.serenehealth.activity.HelpActivity;
import com.serenehealth.activity.IDVerificationActivity;
import com.serenehealth.activity.MedicalCardActivity;
import com.serenehealth.activity.PaymentActivity;
import com.serenehealth.activity.PrivacyPolicyActivity;
import com.serenehealth.activity.ProfileDetailActivity;
import com.serenehealth.activity.SuggestionFeedbackActivity;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.FragmentProfileBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.activity.LoginActivity;
import com.serenehealth.util.SPUtil;

/**
 * 个人中心 Fragment
 * 展示用户信息（头像、姓名、手机号、会员等级），
 * 提供预约记录、身份证核验等菜单入口，支持退出登录。
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
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
        // 患者用户不显示后台管理入口
        if (!SPUtil.isAdminLoggedIn()) {
            binding.menuAdmin.setVisibility(View.GONE);
        }

        // 未登录时显示登录引导
        if (!SPUtil.isLoggedIn()) {
            binding.tvUserName.setText("请先登录");
            binding.tvUserPhone.setText("点击登录使用完整功能");
            binding.tvMemberLevel.setText("");
            binding.tvUserName.setOnClickListener(v -> {
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            });
            return;
        }

        long userId = SPUtil.getCurrentUserId();
        if (userId <= 0) return;

        User user = DBHelper.getInstance(requireContext()).getUserDao().queryUserById(userId);
        if (user == null) return;

        // 显示用户真实姓名
        binding.tvUserName.setText(user.getRealName());

        // 手机号中间四位脱敏显示
        String phone = user.getPhone();
        if (phone != null && phone.length() >= 7) {
            String maskedPhone = phone.substring(0, 3) + "****" + phone.substring(7);
            binding.tvUserPhone.setText(maskedPhone);
        } else {
            binding.tvUserPhone.setText(phone);
        }

        // 显示会员等级（默认"普通会员"）
        String memberLevel = user.getMemberLevel();
        if (memberLevel == null || memberLevel.isEmpty()) {
            memberLevel = getString(R.string.profile_member_normal);
        }
        binding.tvMemberLevel.setText(memberLevel);
    }

    // ==================== 事件监听 ====================

    private void setListeners() {
        // B1. 预约记录
        binding.menuAppointmentRecord.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), AppointmentRecordActivity.class));
        });

        // B2. 医保卡
        binding.menuMedicalCard.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), MedicalCardActivity.class));
        });

        binding.menuPayment.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), PaymentActivity.class));
        });

        // B3. 身份证核验
        binding.menuIdVerification.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), IDVerificationActivity.class));
        });

        // B4. 满意度评价
        binding.menuFeedback.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), FeedbackActivity.class));
        });

        // B5. 意见反馈
        binding.menuSuggestion.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), SuggestionFeedbackActivity.class));
        });

        // B6. 使用帮助
        binding.menuHelp.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), HelpActivity.class)));

        // B7. 隐私政策
        binding.menuPrivacy.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class)));

        binding.userHeaderCard.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), ProfileDetailActivity.class));
        });

        // B8. 退出登录
        binding.menuLogout.setOnClickListener(v -> showLogoutDialog());

        // B9. 后台管理
        binding.menuAdmin.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), LoginActivity.class)));
    }

    /**
     * 检查登录状态，未登录则跳转到登录页
     * @return true 已登录，false 已跳转到登录页
     */
    private boolean checkLogin() {
        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return false;
        }
        return true;
    }

    // ==================== 退出登录 ====================

    /**
     * 显示退出登录确认对话框
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout)
                .setMessage(R.string.profile_logout_confirm)
                .setPositiveButton(R.string.message_center_dialog_confirm, (dialog, which) -> {
                    SPUtil.setLoggedIn(false);
                    SPUtil.clear();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    // ==================== 生命周期 ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
