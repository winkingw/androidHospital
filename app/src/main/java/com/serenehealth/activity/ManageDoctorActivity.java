package com.serenehealth.activity;

import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;

import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.databinding.ActivityManageDoctorBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.db.DoctorDao;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorActivity extends AppCompatActivity {

    private ActivityManageDoctorBinding binding;
    private DBHelper dbHelper;
    private DoctorDao doctorDao;
    private List<Department> departmentList;
    private final List<Doctor> doctorList = new ArrayList<>();
    private DoctorAdapter adapter;
    private long selectedDepartmentId = -1; // -1 means all

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = DBHelper.getInstance(this);
        doctorDao = dbHelper.getDoctorDao();
        initData();
        setListeners();
    }

    private void initData() {
        // Load all departments into spinner
        departmentList = dbHelper.getDepartmentDao().queryAllDepartments();
        List<String> deptNames = new ArrayList<>();
        deptNames.add("全部科室");
        for (Department dept : departmentList) {
            deptNames.add(dept.getDeptName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deptNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDepartment.setAdapter(spinnerAdapter);

        // Load all doctors initially
        loadDoctors(-1);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAdd.setOnClickListener(v -> {
            if (selectedDepartmentId == -1) {
                Toast.makeText(this, "请先选择科室", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddDoctorDialog();
        });

        binding.spDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDepartmentId = -1;
                } else {
                    selectedDepartmentId = departmentList.get(position - 1).getId();
                }
                loadDoctors(selectedDepartmentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadDoctors(long deptId) {
        doctorList.clear();
        if (deptId == -1) {
            doctorList.addAll(getAllDoctors());
        } else {
            List<Doctor> list = doctorDao.queryDoctorsByDepartment(deptId);
            if (list != null) {
                doctorList.addAll(list);
            }
        }

        if (adapter == null) {
            binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(this));
            adapter = new DoctorAdapter();
            binding.rvDoctorList.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        updateEmptyState();
    }

    /**
     * Query all doctors (including inactive ones) for the "all" view.
     */
    private List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToDoctor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private Doctor cursorToDoctor(Cursor cursor) {
        Doctor doctor = new Doctor();
        doctor.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        doctor.setDepartmentId(cursor.getLong(cursor.getColumnIndexOrThrow("department_id")));
        doctor.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
        if (!cursor.isNull(cursor.getColumnIndex("gender"))) {
            doctor.setGender(cursor.getInt(cursor.getColumnIndex("gender")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("title"))) {
            doctor.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("introduction"))) {
            doctor.setIntroduction(cursor.getString(cursor.getColumnIndex("introduction")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("status"))) {
            doctor.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            doctor.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        return doctor;
    }

    private void updateEmptyState() {
        boolean empty = doctorList.isEmpty();
        binding.rvDoctorList.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showAddDoctorDialog() {
        LinearLayout layout = buildDoctorFormLayout(null);
        EditText etName = layout.findViewWithTag("et_name");
        EditText etTitle = layout.findViewWithTag("et_title");
        EditText etIntro = layout.findViewWithTag("et_intro");
        Spinner spGender = layout.findViewWithTag("sp_gender");

        new AlertDialog.Builder(this)
                .setTitle("添加医生")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入医生姓名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Doctor doctor = new Doctor();
                    doctor.setDoctorName(name);
                    doctor.setTitle(etTitle.getText().toString().trim());
                    doctor.setIntroduction(etIntro.getText().toString().trim());
                    doctor.setGender(spGender.getSelectedItemPosition() == 0 ? 1 : 2);
                    doctor.setDepartmentId(selectedDepartmentId);
                    doctor.setStatus(1);
                    doctor.setSortNo(0);
                    long id = doctorDao.insertDoctor(doctor);
                    if (id > 0) {
                        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                        loadDoctors(selectedDepartmentId);
                    } else {
                        Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditDoctorDialog(Doctor doctor) {
        LinearLayout layout = buildDoctorFormLayout(doctor);
        EditText etName = layout.findViewWithTag("et_name");
        EditText etTitle = layout.findViewWithTag("et_title");
        EditText etIntro = layout.findViewWithTag("et_intro");
        Spinner spGender = layout.findViewWithTag("sp_gender");

        new AlertDialog.Builder(this)
                .setTitle("编辑医生")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入医生姓名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    doctor.setDoctorName(name);
                    doctor.setTitle(etTitle.getText().toString().trim());
                    doctor.setIntroduction(etIntro.getText().toString().trim());
                    doctor.setGender(spGender.getSelectedItemPosition() == 0 ? 1 : 2);
                    int rows = doctorDao.updateDoctor(doctor);
                    if (rows > 0) {
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                        loadDoctors(selectedDepartmentId);
                    } else {
                        Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * Build a vertical layout containing EditText fields and a gender Spinner.
     * If doctor is not null, pre-fill the fields.
     */
    private LinearLayout buildDoctorFormLayout(Doctor doctor) {
        float density = getResources().getDisplayMetrics().density;
        int paddingH = (int) (24 * density);
        int paddingT = (int) (8 * density);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(paddingH, paddingT, paddingH, 0);

        // --- helper values for styled EditText fields ---
        int etPadH = (int) (24 * density);
        int etPadV = (int) (16 * density);
        int fieldMarginBottom = (int) (16 * density);

        // Name
        EditText etName = new EditText(this);
        etName.setTag("et_name");
        etName.setHint("姓名");
        etName.setText(doctor != null ? doctor.getDoctorName() : "");
        etName.setPadding(etPadH, etPadV, etPadH, etPadV);
        etName.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams etNameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etNameLp.bottomMargin = fieldMarginBottom;
        etName.setLayoutParams(etNameLp);
        layout.addView(etName);

        // Title
        EditText etTitle = new EditText(this);
        etTitle.setTag("et_title");
        etTitle.setHint("职称");
        etTitle.setText(doctor != null && doctor.getTitle() != null ? doctor.getTitle() : "");
        etTitle.setPadding(etPadH, etPadV, etPadH, etPadV);
        etTitle.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams etTitleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etTitleLp.bottomMargin = fieldMarginBottom;
        etTitle.setLayoutParams(etTitleLp);
        layout.addView(etTitle);

        // Introduction
        EditText etIntro = new EditText(this);
        etIntro.setTag("et_intro");
        etIntro.setHint("简介");
        etIntro.setLines(3);
        etIntro.setText(doctor != null && doctor.getIntroduction() != null ? doctor.getIntroduction() : "");
        etIntro.setPadding(etPadH, etPadV, etPadH, etPadV);
        etIntro.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams etIntroLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etIntroLp.bottomMargin = fieldMarginBottom;
        etIntro.setLayoutParams(etIntroLp);
        layout.addView(etIntro);

        // Gender
        LinearLayout genderRow = new LinearLayout(this);
        genderRow.setOrientation(LinearLayout.HORIZONTAL);
        genderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvGenderLabel = new TextView(this);
        tvGenderLabel.setText("性别：");
        tvGenderLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        genderRow.addView(tvGenderLabel);

        Spinner spGender = new Spinner(this);
        spGender.setTag("sp_gender");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"男", "女"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);
        if (doctor != null) {
            spGender.setSelection(doctor.getGender() == 1 ? 0 : 1);
        }
        genderRow.addView(spGender);

        layout.addView(genderRow);

        return layout;
    }

    private void toggleDoctorStatus(Doctor doctor) {
        String msg;
        if (doctor.getStatus() == 1) {
            doctorDao.disableDoctor(doctor.getId());
            msg = "医生已停用";
            doctor.setStatus(0);
        } else {
            doctor.setStatus(1);
            doctorDao.updateDoctor(doctor);
            msg = "医生已启用";
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        loadDoctors(selectedDepartmentId);
    }

    // ==================== Adapter ====================

    private class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            float density = getResources().getDisplayMetrics().density;

            LinearLayout root = new LinearLayout(ManageDoctorActivity.this);
            root.setOrientation(LinearLayout.HORIZONTAL);
            root.setGravity(Gravity.CENTER_VERTICAL);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (8 * density);
            root.setLayoutParams(lp);
            int pad = (int) (16 * density);
            root.setPadding(pad, (int) (12 * density), pad, (int) (12 * density));

            // selectableItemBackground for click feedback
            int[] attrs = new int[]{android.R.attr.selectableItemBackground};
            TypedArray ta = ManageDoctorActivity.this.obtainStyledAttributes(attrs);
            int bgRes = ta.getResourceId(0, 0);
            ta.recycle();
            root.setBackgroundResource(bgRes);

            // Left: name + title
            LinearLayout textLayout = new LinearLayout(ManageDoctorActivity.this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(ManageDoctorActivity.this);
            tvName.setId(View.generateViewId());
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(
                    ContextCompat.getColor(ManageDoctorActivity.this, R.color.on_surface));
            textLayout.addView(tvName);

            TextView tvTitle = new TextView(ManageDoctorActivity.this);
            tvTitle.setId(View.generateViewId());
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvTitle.setTextColor(
                    ContextCompat.getColor(ManageDoctorActivity.this, R.color.on_surface_variant));
            textLayout.addView(tvTitle);

            root.addView(textLayout);

            // Right: status badge
            TextView tvStatus = new TextView(ManageDoctorActivity.this);
            tvStatus.setId(View.generateViewId());
            tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvStatus.setTypeface(null, Typeface.BOLD);
            root.addView(tvStatus);

            root.setClickable(true);
            root.setFocusable(true);
            return new ViewHolder(root, tvName, tvTitle, tvStatus);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Doctor doctor = doctorList.get(position);
            holder.bind(doctor);
        }

        @Override
        public int getItemCount() {
            return doctorList.size();
        }

        // ==================== ViewHolder ====================

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvName;
            private final TextView tvTitle;
            private final TextView tvStatus;

            ViewHolder(View itemView, TextView tvName, TextView tvTitle, TextView tvStatus) {
                super(itemView);
                this.tvName = tvName;
                this.tvTitle = tvTitle;
                this.tvStatus = tvStatus;
            }

            void bind(Doctor doctor) {
                tvName.setText(doctor.getDoctorName());
                tvTitle.setText(doctor.getTitle() != null ? doctor.getTitle() : "");

                if (doctor.getStatus() == 1) {
                    tvStatus.setText("在职");
                    tvStatus.setTextColor(
                            ContextCompat.getColor(ManageDoctorActivity.this, R.color.status_visited));
                } else {
                    tvStatus.setText("停用");
                    tvStatus.setTextColor(
                            ContextCompat.getColor(ManageDoctorActivity.this, R.color.badge_red));
                }

                itemView.setOnClickListener(v -> showEditDoctorDialog(doctor));

                itemView.setOnLongClickListener(v -> {
                    toggleDoctorStatus(doctor);
                    return true;
                });
            }
        }
    }
}
