package com.serenehealth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.R;
import com.serenehealth.adapter.PaymentOrderAdapter;
import com.serenehealth.bean.MedicalCard;
import com.serenehealth.bean.PaymentOrder;
import com.serenehealth.databinding.ActivityPaymentBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private DBHelper dbHelper;
    private PaymentOrderAdapter adapter;
    private String currentStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
        loadOrders();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        adapter = new PaymentOrderAdapter(new PaymentOrderAdapter.OnPaymentActionListener() {
            @Override
            public void onMockPay(PaymentOrder order) {
                payByMock(order);
            }

            @Override
            public void onMedicalCardPay(PaymentOrder order) {
                payByMedicalCard(order);
            }
        });
        binding.rvPayment.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPayment.setAdapter(adapter);
        binding.groupFilter.check(R.id.btn_filter_all);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.groupFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_filter_unpaid) {
                currentStatus = "UNPAID";
            } else if (checkedId == R.id.btn_filter_paid) {
                currentStatus = "PAID";
            } else {
                currentStatus = "";
            }
            loadOrders();
        });
    }

    private void loadOrders() {
        List<PaymentOrder> allOrders = dbHelper.getPaymentOrderDao().queryOrdersByUser(SPUtil.getCurrentUserId());
        List<PaymentOrder> displayOrders = new ArrayList<>();
        for (PaymentOrder order : allOrders) {
            if (currentStatus == null || currentStatus.isEmpty()
                    || currentStatus.equals(order.getOrderStatus())) {
                displayOrders.add(order);
            }
        }
        adapter.setData(displayOrders);
        boolean empty = displayOrders.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvPayment.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void payByMock(PaymentOrder order) {
        int rows = dbHelper.getPaymentOrderDao().payOrder(order.getId(), "MOCK_PAY");
        Toast.makeText(this, rows > 0 ? "支付成功" : "支付失败", Toast.LENGTH_SHORT).show();
        loadOrders();
    }

    private void payByMedicalCard(PaymentOrder order) {
        MedicalCard card = dbHelper.getMedicalCardDao().queryByUserId(SPUtil.getCurrentUserId());
        if (card == null) {
            Toast.makeText(this, "请先绑定医保卡", Toast.LENGTH_SHORT).show();
            return;
        }
        if (card.getBalance() < order.getAmount()) {
            Toast.makeText(this, "医保卡余额不足", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = dbHelper.getPaymentOrderDao().payOrderWithMedicalCard(order.getId(), card);
        Toast.makeText(this, success ? "医保卡支付成功" : "医保卡支付失败", Toast.LENGTH_SHORT).show();
        loadOrders();
    }
}
