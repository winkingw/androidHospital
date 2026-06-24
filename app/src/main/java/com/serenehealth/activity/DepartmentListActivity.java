package com.serenehealth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.serenehealth.R;
import com.serenehealth.adapter.DoctorAdapter;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.databinding.ActivityDepartmentListBinding;
import com.serenehealth.db.DBHelper;

import java.util.List;

public class DepartmentListActivity extends AppCompatActivity {

    private ActivityDepartmentListBinding binding;
    private DBHelper dbHelper;
    private long departmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepartmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        departmentId = getIntent().getLongExtra("department_id", -1);

        initData();
        setListeners();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);

        // 查询科室名称
        String deptName = null;
        if (departmentId != -1) {
            Department department = dbHelper.getDepartmentDao().queryDepartmentById(departmentId);
            if (department != null) {
                deptName = department.getDeptName();
            }
        }

        // 设置标题
        if (deptName != null) {
            binding.tvTitle.setText(deptName);
        } else {
            binding.tvTitle.setText(R.string.department_doctor_list_title);
        }

        // 查询医生列表
        List<Doctor> doctorList;
        if (departmentId != -1) {
            doctorList = dbHelper.getDoctorDao().queryDoctorsByDepartment(departmentId);
        } else {
            doctorList = java.util.Collections.emptyList();
        }

        // 设置 RecyclerView
        binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(this));
        if (doctorList.isEmpty()) {
            binding.rvDoctorList.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvDoctorList.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);

            DoctorAdapter adapter = new DoctorAdapter(doctorList, doctorId -> {
                // DoctorDetailActivity 尚未实现，预留跳转
                // TODO: 当 DoctorDetailActivity 实现后，取消下方注释并移除 Toast
                // Intent intent = new Intent(DepartmentListActivity.this, DoctorDetailActivity.class);
                // intent.putExtra("doctor_id", doctorId);
                // startActivity(intent);
                Toast.makeText(DepartmentListActivity.this,
                        R.string.doctor_detail_developing, Toast.LENGTH_SHORT).show();
            });
            binding.rvDoctorList.setAdapter(adapter);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }
}
