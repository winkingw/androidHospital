package com.serenehealth.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.serenehealth.R;
import com.serenehealth.activity.AppointmentRecordActivity;
import com.serenehealth.activity.FeedbackActivity;
import com.serenehealth.activity.HelpActivity;
import com.serenehealth.activity.IDVerificationActivity;
import com.serenehealth.activity.LoginActivity;
import com.serenehealth.activity.MedicalCardActivity;
import com.serenehealth.activity.PaymentActivity;
import com.serenehealth.activity.PrivacyPolicyActivity;
import com.serenehealth.activity.ProfileDetailActivity;
import com.serenehealth.activity.SuggestionFeedbackActivity;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.FragmentProfileBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

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

    private void initData() {
        if (!SPUtil.isAdminLoggedIn()) {
            binding.menuAdmin.setVisibility(View.GONE);
        }

        if (!SPUtil.isLoggedIn()) {
            binding.tvUserName.setText("请先登录");
            binding.tvUserPhone.setText("点击登录使用完整功能");
            binding.tvRealNameStatus.setText("");
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

        binding.tvUserName.setText(user.getRealName());
        String phone = user.getPhone();
        binding.tvUserPhone.setText(phone == null || phone.isEmpty() ? "手机号：未填写" : "手机号：" + phone);
        binding.tvRealNameStatus.setText(user.isRealNameVerified() ? "实名状态：已实名" : "实名状态：未实名");

        String memberLevel = user.getMemberLevel();
        if (memberLevel == null || memberLevel.isEmpty()) {
            memberLevel = getString(R.string.profile_member_normal);
        }
        binding.tvMemberLevel.setText(memberLevel);
    }

    private void setListeners() {
        binding.menuAppointmentRecord.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), AppointmentRecordActivity.class));
        });

        binding.menuMedicalCard.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), MedicalCardActivity.class));
        });

        binding.menuPayment.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), PaymentActivity.class));
        });

        binding.menuIdVerification.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), IDVerificationActivity.class));
        });

        binding.menuFeedback.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), FeedbackActivity.class));
        });

        binding.menuSuggestion.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), SuggestionFeedbackActivity.class));
        });

        binding.menuHelp.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), HelpActivity.class)));

        binding.menuPrivacy.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class)));

        binding.userHeaderCard.setOnClickListener(v -> {
            if (!checkLogin()) return;
            startActivity(new Intent(requireActivity(), ProfileDetailActivity.class));
        });

        binding.menuLogout.setOnClickListener(v -> showLogoutDialog());

        binding.menuAdmin.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), LoginActivity.class)));
    }

    private boolean checkLogin() {
        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return false;
        }
        return true;
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
