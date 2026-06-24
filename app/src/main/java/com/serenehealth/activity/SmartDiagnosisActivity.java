package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.SymptomDepartmentRule;
import com.serenehealth.databinding.ActivitySmartDiagnosisBinding;
import com.serenehealth.db.DBHelper;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SmartDiagnosisActivity extends AppCompatActivity {

    // ==================== 1. ViewBinding ====================
    private ActivitySmartDiagnosisBinding binding;

    // ==================== 2. 成员变量 ====================
    private DBHelper dbHelper;
    private SmartDiagnosisAdapter adapter;
    private List<SmartDiagnosisResult> resultList;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ==================== 3. 生命周期 ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartDiagnosisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // ==================== 4. 初始化数据 ====================
    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        resultList = new ArrayList<>();
        adapter = new SmartDiagnosisAdapter();

        binding.rvSearchResult.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResult.setAdapter(adapter);
    }

    // ==================== 5. 设置监听 ====================
    private void setListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 搜索按钮
        binding.btnSearch.setOnClickListener(v -> doSearch());

        // 键盘搜索键
        binding.etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // 快捷标签多选监听
        binding.chipGroupSymptoms.setOnCheckedStateChangeListener((group, checkedIds) -> {
            StringBuilder sb = new StringBuilder();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(chip.getText().toString());
                }
            }
            String keyword = sb.toString().trim();
            binding.etSearch.setText(keyword);
            if (!TextUtils.isEmpty(keyword)) {
                binding.etSearch.setSelection(keyword.length());
            }
            doSearch();
        });
    }

    // ==================== 6. 业务方法 ====================

    /**
     * 执行搜索：获取关键词，在后台线程查询数据库
     */
    private void doSearch() {
        String keyword = binding.etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(this, R.string.smart_search_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        // 后台查询
        new Thread(() -> {
            List<SmartDiagnosisResult> results = queryResults(keyword);
            mainHandler.post(() -> {
                if (!isFinishing()) {
                    displayResults(results);
                }
            });
        }).start();
    }

    /**
     * 查询匹配的科室结果
     */
    private List<SmartDiagnosisResult> queryResults(String keyword) {
        List<SmartDiagnosisResult> results = new ArrayList<>();
        List<SymptomDepartmentRule> rules = dbHelper.getSymptomDepartmentRuleDao()
                .queryBySymptom(keyword);

        for (SymptomDepartmentRule rule : rules) {
            Department department = dbHelper.getDepartmentDao()
                    .queryDepartmentById(rule.getDepartmentId());
            if (department != null) {
                SmartDiagnosisResult result = new SmartDiagnosisResult();
                result.rule = rule;
                result.department = department;
                results.add(result);
            }
        }
        return results;
    }

    /**
     * 展示搜索结果
     */
    private void displayResults(List<SmartDiagnosisResult> results) {
        resultList.clear();
        resultList.addAll(results);

        if (results.isEmpty()) {
            binding.rvSearchResult.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvSearchResult.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 点击结果项，跳转到科室医生列表
     */
    private void onResultItemClick(SmartDiagnosisResult item) {
        long departmentId = item.department.getId();
        Intent intent = new Intent(this, DepartmentListActivity.class);
        intent.putExtra("department_id", departmentId);
        startActivity(intent);
        Toast.makeText(this, R.string.smart_jump_department, Toast.LENGTH_SHORT).show();
    }

    // ==================== 数据模型 ====================

    /**
     * 搜索结果数据模型：症状匹配规则 + 科室信息
     */
    private static class SmartDiagnosisResult {
        SymptomDepartmentRule rule;
        Department department;
    }

    // ==================== RecyclerView 适配器 ====================

    private class SmartDiagnosisAdapter extends RecyclerView.Adapter<SmartDiagnosisAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(
                    R.layout.item_smart_diagnosis_result, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SmartDiagnosisResult item = resultList.get(position);
            Department department = item.department;
            SymptomDepartmentRule rule = item.rule;

            holder.tvDeptName.setText(department.getDeptName());

            String description = department.getDescription();
            if (!TextUtils.isEmpty(description)) {
                holder.tvDeptDescription.setText(description);
                holder.tvDeptDescription.setVisibility(View.VISIBLE);
            } else {
                holder.tvDeptDescription.setVisibility(View.GONE);
            }

            String recommendReason = rule.getRecommendReason();
            if (!TextUtils.isEmpty(recommendReason)) {
                holder.tvRecommendReason.setText(recommendReason);
                holder.tvRecommendReason.setVisibility(View.VISIBLE);
            } else {
                holder.tvRecommendReason.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> onResultItemClick(item));
        }

        @Override
        public int getItemCount() {
            return resultList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDeptName;
            TextView tvDeptDescription;
            TextView tvRecommendReason;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDeptName = itemView.findViewById(R.id.tv_dept_name);
                tvDeptDescription = itemView.findViewById(R.id.tv_dept_description);
                tvRecommendReason = itemView.findViewById(R.id.tv_recommend_reason);
            }
        }
    }
}
