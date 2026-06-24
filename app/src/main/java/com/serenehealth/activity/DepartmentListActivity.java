package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.adapter.DepartmentAdapter;
import com.serenehealth.bean.Department;
import com.serenehealth.databinding.ActivityDepartmentListBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.db.DepartmentDao;

import java.util.List;

public class DepartmentListActivity extends AppCompatActivity {

    // ==================== 1. ViewBinding ====================
    private ActivityDepartmentListBinding binding;

    // ==================== 2. 成员变量 ====================
    private DepartmentDao departmentDao;
    private DepartmentAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long SEARCH_DELAY_MS = 300;

    // ==================== 3. 生命周期 ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepartmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
        loadDepartments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchHandler.removeCallbacksAndMessages(null);
    }

    // ==================== 4. 初始化数据 ====================
    private void initData() {
        departmentDao = DBHelper.getInstance(this).getDepartmentDao();

        adapter = new DepartmentAdapter();
        adapter.setOnItemClickListener(new DepartmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Department department) {
                Intent intent = new Intent(DepartmentListActivity.this,
                        DoctorListActivity.class);
                intent.putExtra("department_id", department.getId());
                intent.putExtra("department_name", department.getDeptName());
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // ==================== 5. 设置监听 ====================
    private void setListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 搜索框文字变化监听（防抖）
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performSearch(s.toString().trim());
                    }
                }, SEARCH_DELAY_MS);
            }
        });
    }

    // ==================== 6. 业务方法 ====================

    /**
     * 加载全部科室
     */
    private void loadDepartments() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Department> list = departmentDao.queryAllDepartments();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            adapter.setData(list);
                            updateEmptyView();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 按关键字搜索科室
     */
    private void performSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            loadDepartments();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Department> list = departmentDao.searchDepartments(keyword);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            adapter.setData(list);
                            updateEmptyView();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 更新空视图显示状态
     */
    private void updateEmptyView() {
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
