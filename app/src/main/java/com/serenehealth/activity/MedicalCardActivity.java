package com.serenehealth.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.adapter.MedicalCardRecordAdapter;
import com.serenehealth.bean.MedicalCard;
import com.serenehealth.bean.MedicalCardRecord;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityMedicalCardBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalCardActivity extends AppCompatActivity {

    private ActivityMedicalCardBinding binding;
    private DBHelper dbHelper;
    private MedicalCardRecordAdapter adapter;
    private User user;
    private MedicalCard card;
    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
        loadCard();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        user = dbHelper.getUserDao().queryUserById(SPUtil.getCurrentUserId());
        adapter = new MedicalCardRecordAdapter();
        binding.rvRecord.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecord.setAdapter(adapter);
        if (user != null) {
            binding.etHolderName.setText(user.getRealName());
            binding.etIdCard.setText(user.getIdCardNo());
            binding.etPhone.setText(user.getPhone());
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBind.setOnClickListener(v -> bindCard());
        binding.btnUnbind.setOnClickListener(v -> confirmUnbind());
    }

    private void loadCard() {
        card = dbHelper.getMedicalCardDao().queryByUserId(SPUtil.getCurrentUserId());
        boolean bound = card != null;
        binding.cardBind.setVisibility(bound ? View.GONE : View.VISIBLE);
        binding.btnUnbind.setVisibility(bound ? View.VISIBLE : View.GONE);
        if (bound) {
            binding.tvCardStatus.setText("已绑定医保卡");
            binding.tvCardNo.setText("卡号：" + mask(card.getMedicalCardNo(), 4, 4));
            binding.tvHolder.setText(card.getHolderName() + " · " + mask(card.getBindPhone(), 3, 4));
            binding.tvBalance.setText(String.format(Locale.getDefault(), "¥%.2f", card.getBalance()));
            loadRecords(card.getId());
        } else {
            binding.tvCardStatus.setText("暂未绑定医保卡");
            binding.tvCardNo.setText("绑定后可查看卡号与消费记录");
            binding.tvHolder.setText("");
            binding.tvBalance.setText("¥0.00");
            adapter.setData(null);
            binding.tvRecordEmpty.setVisibility(View.VISIBLE);
            binding.rvRecord.setVisibility(View.GONE);
        }
    }

    private void bindCard() {
        if (submitting) {
            return;
        }
        String holderName = binding.etHolderName.getText().toString().trim();
        String idCard = binding.etIdCard.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String cardNo = binding.etCardNo.getText().toString().trim();
        if (holderName.isEmpty() || idCard.isEmpty() || phone.isEmpty() || cardNo.isEmpty()) {
            Toast.makeText(this, "请完整填写医保卡信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("^1\\d{10}$")) {
            Toast.makeText(this, "手机号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!idCard.matches("^\\d{17}[0-9Xx]$")) {
            Toast.makeText(this, "身份证号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        submitting = true;
        binding.btnBind.setEnabled(false);
        MedicalCard newCard = new MedicalCard();
        newCard.setUserId(SPUtil.getCurrentUserId());
        newCard.setHolderName(holderName);
        newCard.setIdCardNo(idCard);
        newCard.setMedicalCardNo(cardNo);
        newCard.setBindPhone(phone);
        newCard.setBalance(5000.00);
        newCard.setValidStart(today());
        newCard.setValidEnd(nextYear());
        newCard.setBindStatus("BOUND");
        long result = dbHelper.getMedicalCardDao().bind(newCard);
        Toast.makeText(this, result > 0 ? "绑定成功" : "绑定失败，卡号可能已被使用", Toast.LENGTH_SHORT).show();
        submitting = false;
        binding.btnBind.setEnabled(true);
        loadCard();
    }

    private void confirmUnbind() {
        new AlertDialog.Builder(this)
                .setTitle("解绑医保卡")
                .setMessage("确定解绑当前医保卡吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    int rows = dbHelper.getMedicalCardDao().unbind(SPUtil.getCurrentUserId());
                    Toast.makeText(this, rows > 0 ? "已解绑" : "解绑失败", Toast.LENGTH_SHORT).show();
                    loadCard();
                })
                .setNegativeButton("返回", null)
                .show();
    }

    private void loadRecords(long cardId) {
        List<MedicalCardRecord> records = dbHelper.getMedicalCardRecordDao().queryByCardId(cardId);
        adapter.setData(records);
        boolean empty = records == null || records.isEmpty();
        binding.tvRecordEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvRecord.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String mask(String text, int prefix, int suffix) {
        if (text == null || text.length() <= prefix + suffix) {
            return text != null ? text : "";
        }
        return text.substring(0, prefix) + "****" + text.substring(text.length() - suffix);
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String nextYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }
}
