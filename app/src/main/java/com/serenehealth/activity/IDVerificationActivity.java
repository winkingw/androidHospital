package com.serenehealth.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.R;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityIdVerificationBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

public class IDVerificationActivity extends AppCompatActivity {

    private static final int REQUEST_FRONT_PHOTO = 1001;
    private static final int REQUEST_BACK_PHOTO = 1002;

    private ActivityIdVerificationBinding binding;
    private DBHelper dbHelper;
    private long currentUserId;

    private boolean frontUploaded = false;
    private boolean backUploaded = false;
    private String frontPhotoUri;
    private String backPhotoUri;
    private String verificationStatus = "PENDING";

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DBHelper.getInstance(this);
        currentUserId = SPUtil.getCurrentUserId();

        loadCurrentUser();
        updateStatusUI();
        setListeners();
        showNotVerifiedDialogIfNeeded();
    }

    private void loadCurrentUser() {
        if (currentUserId <= 0) {
            return;
        }
        User user = dbHelper.getUserDao().queryUserById(currentUserId);
        if (user == null) {
            return;
        }

        binding.etRealName.setText(user.getRealName());
        binding.etIdCardNo.setText(user.getIdCardNo());

        frontPhotoUri = user.getIdCardFrontUri();
        backPhotoUri = user.getIdCardBackUri();
        if (frontPhotoUri != null && !frontPhotoUri.isEmpty()) {
            frontUploaded = true;
            showSelectedImage(binding.ivFrontPreview, Uri.parse(frontPhotoUri));
            binding.tvFrontLabel.setText(R.string.id_front_selected);
        }
        if (backPhotoUri != null && !backPhotoUri.isEmpty()) {
            backUploaded = true;
            showSelectedImage(binding.ivBackPreview, Uri.parse(backPhotoUri));
            binding.tvBackLabel.setText(R.string.id_back_selected);
        }

        if (user.isRealNameVerified()) {
            verificationStatus = "APPROVED";
            frontUploaded = true;
            backUploaded = true;
            binding.tvFrontLabel.setText(R.string.id_front_selected);
            binding.tvBackLabel.setText(R.string.id_back_selected);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardFront.setOnClickListener(v -> {
            if ("APPROVED".equals(verificationStatus)) {
                Toast.makeText(this, R.string.id_already_verified, Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker(REQUEST_FRONT_PHOTO);
        });

        binding.cardBack.setOnClickListener(v -> {
            if ("APPROVED".equals(verificationStatus)) {
                Toast.makeText(this, R.string.id_already_verified, Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker(REQUEST_BACK_PHOTO);
        });

        binding.btnSubmit.setOnClickListener(v -> submitVerification());
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }

        if (requestCode == REQUEST_FRONT_PHOTO) {
            frontUploaded = true;
            frontPhotoUri = uri.toString();
            showSelectedImage(binding.ivFrontPreview, uri);
            binding.tvFrontLabel.setText(R.string.id_front_selected);
            Toast.makeText(this, R.string.id_front_photo_selected, Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_BACK_PHOTO) {
            backUploaded = true;
            backPhotoUri = uri.toString();
            showSelectedImage(binding.ivBackPreview, uri);
            binding.tvBackLabel.setText(R.string.id_back_selected);
            Toast.makeText(this, R.string.id_back_photo_selected, Toast.LENGTH_SHORT).show();
        }
        updateStatusUI();
    }

    private void showSelectedImage(ImageView imageView, Uri uri) {
        imageView.setImageTintList((ColorStateList) null);
        imageView.setImageURI(uri);
    }

    private void submitVerification() {
        String realName = binding.etRealName.getText().toString().trim();
        String idCardNo = binding.etIdCardNo.getText().toString().trim();
        if (realName.isEmpty()) {
            Toast.makeText(this, R.string.id_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (idCardNo.isEmpty()) {
            Toast.makeText(this, R.string.id_card_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (idCardNo.length() != 15 && idCardNo.length() != 18) {
            Toast.makeText(this, "身份证号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!frontUploaded || !backUploaded) {
            Toast.makeText(this, R.string.id_photo_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUserId <= 0) {
            Toast.makeText(this, R.string.appointment_fail_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        verificationStatus = "REVIEWING";
        updateStatusUI();

        Toast.makeText(this, R.string.id_submitted_reviewing, Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            int rows = dbHelper.getUserDao()
                    .updateRealNameVerification(currentUserId, realName, idCardNo,
                            frontPhotoUri, backPhotoUri);
            if (rows > 0) {
                verificationStatus = "APPROVED";
                updateStatusUI();
                Toast.makeText(this, R.string.id_verified_success, Toast.LENGTH_SHORT).show();
            } else {
                verificationStatus = "PENDING";
                updateStatusUI();
                Toast.makeText(this, R.string.appointment_fail_msg, Toast.LENGTH_SHORT).show();
            }
        }, 800);
    }

    private void updateStatusUI() {
        switch (verificationStatus) {
            case "PENDING":
                binding.tvVerificationStatus.setText(getString(R.string.id_status_pending));
                binding.tvVerificationStatus.setTextColor(getColor(R.color.on_surface_variant));
                binding.progressBar.setVisibility(View.GONE);
                binding.cardFront.setEnabled(true);
                binding.cardBack.setEnabled(true);
                binding.etRealName.setEnabled(true);
                binding.etIdCardNo.setEnabled(true);
                binding.btnSubmit.setEnabled(frontUploaded && backUploaded);
                break;

            case "REVIEWING":
                binding.tvVerificationStatus.setText(getString(R.string.id_status_reviewing));
                binding.tvVerificationStatus.setTextColor(getColor(R.color.primary_container));
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.cardFront.setEnabled(false);
                binding.cardBack.setEnabled(false);
                binding.etRealName.setEnabled(false);
                binding.etIdCardNo.setEnabled(false);
                binding.btnSubmit.setEnabled(false);
                break;

            case "APPROVED":
                binding.tvVerificationStatus.setText(getString(R.string.id_status_approved));
                binding.tvVerificationStatus.setTextColor(getColor(R.color.primary));
                binding.progressBar.setVisibility(View.GONE);
                binding.cardFront.setEnabled(false);
                binding.cardBack.setEnabled(false);
                binding.etRealName.setEnabled(false);
                binding.etIdCardNo.setEnabled(false);
                binding.btnSubmit.setEnabled(false);
                break;
        }
    }

    private void showNotVerifiedDialogIfNeeded() {
        if (getIntent().getBooleanExtra("show_not_verified_dialog", false)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.appointment_need_real_name)
                    .setPositiveButton(R.string.message_center_dialog_confirm, null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
