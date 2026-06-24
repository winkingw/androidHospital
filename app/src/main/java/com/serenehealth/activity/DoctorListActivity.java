package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.adapter.DoctorAdapter;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.databinding.ActivityDoctorListBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.db.DepartmentDao;
import com.serenehealth.db.DoctorDao;
import com.serenehealth.db.DoctorScheduleDao;
import com.serenehealth.R;
import com.serenehealth.db.RegisterSourceDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorListActivity extends AppCompatActivity {

    // ==================== 1. ViewBinding ====================
    private ActivityDoctorListBinding binding;

    // ==================== 2. 成员变量 ====================
    private DoctorDao doctorDao;
    private DepartmentDao departmentDao;
    private DoctorScheduleDao scheduleDao;
    private RegisterSourceDao sourceDao;
    private DoctorAdapter adapter;
    private long departmentId;
    private String departmentName;
    private List<Doctor> allDoctors = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long SEARCH_DELAY_MS = 300;

    // 筛选栏
    private int selectedFilterIndex = 0;
    private TextView[] filterTabs;
    private String[] filterTitles;

    // ==================== 3. 生命周期 ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        departmentId = getIntent().getLongExtra("department_id", -1);
        departmentName = getIntent().getStringExtra("department_name");

        initData();
        setListeners();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchHandler.removeCallbacksAndMessages(null);
    }

    // ==================== 4. 初始化数据 ====================
    private void initData() {
        doctorDao = DBHelper.getInstance(this).getDoctorDao();
        departmentDao = DBHelper.getInstance(this).getDepartmentDao();
        scheduleDao = DBHelper.getInstance(this).getDoctorScheduleDao();
        sourceDao = DBHelper.getInstance(this).getRegisterSourceDao();

        // 标题栏显示科室名
        if (departmentName != null && !departmentName.isEmpty()) {
            binding.tvTitle.setText(departmentName + " — " + getString(R.string.doctor_list_title));
        }

        adapter = new DoctorAdapter();
        adapter.setOnItemClickListener(new DoctorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Doctor doctor) {
                Intent intent = new Intent(DoctorListActivity.this,
                        DoctorDetailActivity.class);
                intent.putExtra("doctor_id", doctor.getId());
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 初始化筛选栏标题
        filterTitles = new String[]{
                getString(R.string.department_filter_all),
                getString(R.string.doctor_title_chief),
                getString(R.string.doctor_title_deputy),
                getString(R.string.doctor_title_attending)
        };

        // 初始化筛选栏
        filterTabs = new TextView[]{
                binding.tvFilterAll,
                binding.tvFilterChief,
                binding.tvFilterDeputy,
                binding.tvFilterAttending
        };
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

        // 筛选栏点击
        for (int i = 0; i < filterTabs.length; i++) {
            final int index = i;
            filterTabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedFilterIndex != index) {
                        selectedFilterIndex = index;
                        updateFilterTabs();
                        filterDoctors();
                    }
                }
            });
        }
    }

    // ==================== 6. 业务方法 ====================

    /**
     * 更新筛选栏选中状态
     */
    private void updateFilterTabs() {
        for (int i = 0; i < filterTabs.length; i++) {
            if (i == selectedFilterIndex) {
                filterTabs[i].setBackgroundResource(R.drawable.shape_tab_selected);
                filterTabs[i].setTextColor(getColor(R.color.white));
            } else {
                filterTabs[i].setBackgroundResource(R.drawable.shape_tab_unselected);
                filterTabs[i].setTextColor(getColor(R.color.on_surface_variant));
            }
        }
    }

    /**
     * 加载数据：科室名 + 医生列表 + 号源状态
     */
    private void loadData() {
        if (departmentName == null || departmentName.isEmpty()) {
            loadDepartmentName();
        }
        loadDoctors();
    }

    /**
     * 加载科室名称，显示在标题栏
     */
    private void loadDepartmentName() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Department department = departmentDao.queryDepartmentById(departmentId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing() && department != null) {
                            departmentName = department.getDeptName();
                            binding.tvTitle.setText(departmentName + " — "
                                    + getString(R.string.doctor_list_title));
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 加载某科室下的医生列表及号源状态
     */
    private void loadDoctors() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Doctor> list = doctorDao.queryDoctorsByDepartment(departmentId);

                // 加载号源状态（未来7天）
                final Map<Long, Boolean> hasSlotMap = new HashMap<>();
                if (list != null && !list.isEmpty()) {
                    List<String> dates = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                    Calendar cal = Calendar.getInstance();
                    for (int i = 0; i < 7; i++) {
                        dates.add(sdf.format(cal.getTime()));
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    for (Doctor doctor : list) {
                        boolean hasSlot = false;
                        for (String date : dates) {
                            List<DoctorSchedule> schedules =
                                    scheduleDao.querySchedulesByDoctor(doctor.getId(), date);
                            if (schedules != null) {
                                for (DoctorSchedule s : schedules) {
                                    List<RegisterSource> sources =
                                            sourceDao.querySourcesBySchedule(s.getId());
                                    if (sources != null) {
                                        for (RegisterSource src : sources) {
                                            if (src.getRemainNum() > 0) {
                                                hasSlot = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (hasSlot) break;
                                }
                            }
                            if (hasSlot) break;
                        }
                        hasSlotMap.put(doctor.getId(), hasSlot);
                    }
                }

                final Map<Long, Boolean> finalHasSlotMap = hasSlotMap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            allDoctors.clear();
                            if (list != null) {
                                allDoctors.addAll(list);
                            }
                            adapter.setHasSlotMap(finalHasSlotMap);

                            // 显示筛选栏
                            boolean hasFilterableTitles = false;
                            for (Doctor d : allDoctors) {
                                String title = d.getTitle();
                                if (title != null && (title.contains("主任") || title.contains("主治"))) {
                                    hasFilterableTitles = true;
                                    break;
                                }
                            }
                            binding.filterBar.setVisibility(hasFilterableTitles ? View.VISIBLE : View.GONE);

                            // 应用当前筛选
                            filterDoctors();
                            updateEmptyView();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 按职称筛选医生
     */
    private void filterDoctors() {
        List<Doctor> filteredList;
        if (selectedFilterIndex == 0) {
            // "全部"
            filteredList = new ArrayList<>(allDoctors);
        } else {
            String targetTitle = filterTitles[selectedFilterIndex];
            filteredList = new ArrayList<>();
            for (Doctor d : allDoctors) {
                if (targetTitle.equals(d.getTitle())) {
                    filteredList.add(d);
                }
            }
        }
        adapter.setData(filteredList);
        updateEmptyView();
    }

    /**
     * 按关键字搜索医生
     */
    private void performSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            filterDoctors();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Doctor> searchResult = doctorDao.searchDoctors(keyword);
                final List<Doctor> filteredList = new ArrayList<Doctor>();
                for (Doctor doctor : searchResult) {
                    if (doctor.getDepartmentId() == departmentId) {
                        // 再按当前筛选过滤
                        if (selectedFilterIndex == 0
                                || filterTitles[selectedFilterIndex].equals(doctor.getTitle())) {
                            filteredList.add(doctor);
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            adapter.setData(filteredList);
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
