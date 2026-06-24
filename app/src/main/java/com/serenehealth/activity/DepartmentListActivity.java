package com.serenehealth.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.R;

/**
 * 科室医生列表页面（功能2预留）
 * TODO 2026-06-24 待功能2开发时完善：根据 department_id 查询并展示科室下的医生列表
 */
public class DepartmentListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO 功能2实现时设置布局
        // setContentView(...);

        long departmentId = getIntent().getLongExtra("department_id", -1);
        Toast.makeText(this, getString(R.string.smart_jump_department)
                + " (department_id=" + departmentId + ")", Toast.LENGTH_SHORT).show();
    }
}
