package com.serenehealth.activity;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;

import com.serenehealth.bean.Department;
import com.serenehealth.databinding.ActivityManageDepartmentBinding;
import com.serenehealth.db.DBHelper;

import java.util.List;

public class ManageDepartmentActivity extends AppCompatActivity {

    private ActivityManageDepartmentBinding binding;
    private DBHelper dbHelper;
    private List<Department> departmentList;
    private DepartmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageDepartmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = DBHelper.getInstance(this);
        initData();
        setListeners();
    }

    private void initData() {
        binding.rvDepartmentList.setLayoutManager(new LinearLayoutManager(this));
        departmentList = dbHelper.getDepartmentDao().queryAllDepartments();
        adapter = new DepartmentAdapter(departmentList);
        binding.rvDepartmentList.setAdapter(adapter);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (departmentList.isEmpty()) {
            binding.rvDepartmentList.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvDepartmentList.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        LinearLayout layout = createFormLayout();
        EditText etName = createEditText("科室名称");
        EditText etCode = createEditText("科室编码");
        EditText etDesc = createEditText("科室描述");
        layout.addView(etName);
        layout.addView(etCode);
        layout.addView(etDesc);

        new AlertDialog.Builder(this)
                .setTitle("新增科室")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "科室名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Department dept = new Department();
                    dept.setDeptName(name);
                    dept.setDeptCode(etCode.getText().toString().trim());
                    dept.setDescription(etDesc.getText().toString().trim());
                    dept.setSortNo(departmentList.size() + 1);
                    long result = dbHelper.getDepartmentDao().insertDepartment(dept);
                    if (result > 0) {
                        Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "新增失败，科室编码可能已存在", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditDialog(Department dept) {
        LinearLayout layout = createFormLayout();
        EditText etName = createEditText("科室名称");
        EditText etCode = createEditText("科室编码");
        EditText etDesc = createEditText("科室描述");

        etName.setText(dept.getDeptName());
        if (dept.getDeptCode() != null) {
            etCode.setText(dept.getDeptCode());
        }
        if (dept.getDescription() != null) {
            etDesc.setText(dept.getDescription());
        }

        layout.addView(etName);
        layout.addView(etCode);
        layout.addView(etDesc);

        new AlertDialog.Builder(this)
                .setTitle("编辑科室")
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "科室名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dept.setDeptName(name);
                    dept.setDeptCode(etCode.getText().toString().trim());
                    dept.setDescription(etDesc.getText().toString().trim());
                    int result = dbHelper.getDepartmentDao().updateDepartment(dept);
                    if (result > 0) {
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteConfirm(Department dept) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除科室\"" + dept.getDeptName() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    int result = dbHelper.getDepartmentDao().deleteDepartment(dept.getId());
                    if (result > 0) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "删除失败，该科室下可能存在关联医生", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void refreshData() {
        departmentList.clear();
        departmentList.addAll(dbHelper.getDepartmentDao().queryAllDepartments());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private LinearLayout createFormLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        layout.setPadding(paddingPx, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()),
                paddingPx, 0);
        return layout;
    }

    private EditText createEditText(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);

        // Add 12dp padding for a proper touch target and visual spacing
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        editText.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        // Apply an outlined background for a polished look
        editText.setBackgroundResource(android.R.drawable.edit_text);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        params.setMargins(0, 0, 0, marginPx);
        editText.setLayoutParams(params);
        return editText;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ==================== Inline Adapter ====================

    private class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

        private final List<Department> list;

        DepartmentAdapter(List<Department> list) {
            this.list = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text1;
            final TextView text2;

            ViewHolder(View itemView, TextView text1, TextView text2) {
                super(itemView);
                this.text1 = text1;
                this.text2 = text2;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            float density = getResources().getDisplayMetrics().density;
            int paddingPx = (int) (16 * density);

            LinearLayout root = new LinearLayout(ManageDepartmentActivity.this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setBackgroundColor(
                    ContextCompat.getColor(ManageDepartmentActivity.this, R.color.white));
            root.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            root.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));

            TextView text1 = new TextView(ManageDepartmentActivity.this);
            text1.setId(android.R.id.text1);
            text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            text1.setTypeface(null, Typeface.BOLD);
            root.addView(text1);

            TextView text2 = new TextView(ManageDepartmentActivity.this);
            text2.setId(android.R.id.text2);
            text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            root.addView(text2);

            // Divider at bottom
            View divider = new View(ManageDepartmentActivity.this);
            divider.setBackgroundColor(
                    ContextCompat.getColor(ManageDepartmentActivity.this, R.color.outline_variant));
            LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) (0.5f * density));
            divider.setLayoutParams(dividerLp);
            root.addView(divider);

            return new ViewHolder(root, text1, text2);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Department dept = list.get(position);

            holder.text1.setText(dept.getDeptName());

            String code = dept.getDeptCode();
            holder.text2.setText(code != null ? code : "");
            holder.text2.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.on_surface_variant));
            holder.text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            holder.itemView.setOnClickListener(v -> showEditDialog(dept));
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteConfirm(dept);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
