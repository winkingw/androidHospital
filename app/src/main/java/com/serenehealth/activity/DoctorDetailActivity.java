package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;
import com.serenehealth.adapter.ScheduleAdapter;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityDoctorDetailBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 医生详情与排班页面（功能3）
 * 展示医生详细信息、Tab切换（简介/出诊）、未来7天日期选择、排班时段及号源槽位
 */
public class DoctorDetailActivity extends AppCompatActivity {

    // ==================== 1. ViewBinding ====================
    private ActivityDoctorDetailBinding binding;

    // ==================== 2. 成员变量 ====================
    private DBHelper dbHelper;
    private Doctor doctor;
    private String departmentName;
    private ScheduleAdapter scheduleAdapter;
    private DatePickerAdapter datePickerAdapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Tab 状态
    private boolean isIntroTabSelected = true;

    // 日期相关
    private List<Date> dateList = new ArrayList<>();
    private int selectedDateIndex = 0;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat queryDateFormat;

    // ==================== 3. 生命周期 ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化日期格式化
        displayDateFormat = new SimpleDateFormat("MM-dd EEE", Locale.CHINA);
        queryDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        // 生成未来7天日期
        generateDateList();

        initData();
        initViews();
        loadDoctorInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // ==================== 4. 初始化 ====================

    /**
     * 生成从今天开始的未来7天日期
     */
    private void generateDateList() {
        dateList.clear();
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        for (int i = 0; i < 7; i++) {
            dateList.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
    }

    private void initViews() {
        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Tab 点击切换
        binding.tvTabIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isIntroTabSelected) {
                    isIntroTabSelected = true;
                    updateTabSelection();
                }
            }
        });

        binding.tvTabSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIntroTabSelected) {
                    isIntroTabSelected = false;
                    updateTabSelection();
                    // 切换到出诊Tab时，如果医生已加载，自动加载排班
                    if (doctor != null) {
                        loadSchedulesForDate(selectedDateIndex);
                    }
                }
            }
        });

        // 初始 Tab 状态
        updateTabSelection();

        // 日期选择器
        datePickerAdapter = new DatePickerAdapter();
        binding.rvDatePicker.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvDatePicker.setAdapter(datePickerAdapter);

        // 排班列表
        scheduleAdapter = new ScheduleAdapter(new ScheduleAdapter.OnSlotClickListener() {
            @Override
            public void onSlotClick(DoctorSchedule schedule, RegisterSource source) {
                navigateToAppointment(schedule, source);
            }
        });
        binding.rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSchedule.setAdapter(scheduleAdapter);
    }

    /**
     * 更新 Tab 选中状态
     */
    private void updateTabSelection() {
        if (isIntroTabSelected) {
            binding.tvTabIntro.setBackgroundResource(R.drawable.shape_tab_selected);
            binding.tvTabIntro.setTextColor(getColor(R.color.white));
            binding.tvTabIntro.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tvTabSchedule.setBackgroundResource(R.drawable.shape_tab_unselected);
            binding.tvTabSchedule.setTextColor(getColor(R.color.on_surface_variant));
            binding.tvTabSchedule.setTypeface(null, android.graphics.Typeface.NORMAL);

            binding.layoutIntroContent.setVisibility(View.VISIBLE);
            binding.layoutScheduleContent.setVisibility(View.GONE);
        } else {
            binding.tvTabSchedule.setBackgroundResource(R.drawable.shape_tab_selected);
            binding.tvTabSchedule.setTextColor(getColor(R.color.white));
            binding.tvTabSchedule.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.tvTabIntro.setBackgroundResource(R.drawable.shape_tab_unselected);
            binding.tvTabIntro.setTextColor(getColor(R.color.on_surface_variant));
            binding.tvTabIntro.setTypeface(null, android.graphics.Typeface.NORMAL);

            binding.layoutIntroContent.setVisibility(View.GONE);
            binding.layoutScheduleContent.setVisibility(View.VISIBLE);
        }
    }

    // ==================== 5. 医生信息加载 ====================

    /**
     * 从 intent 获取 doctor_id 并加载医生信息
     */
    private void loadDoctorInfo() {
        final long doctorId = getIntent().getLongExtra("doctor_id", -1);
        if (doctorId == -1) {
            binding.tvDoctorName.setText(R.string.doctor_detail_title);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                doctor = dbHelper.getDoctorDao().queryDoctorById(doctorId);
                if (doctor != null && doctor.getDepartmentId() > 0) {
                    Department dept = dbHelper.getDepartmentDao()
                            .queryDepartmentById(doctor.getDepartmentId());
                    if (dept != null) {
                        departmentName = dept.getDeptName();
                    }
                }

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            updateDoctorInfo();
                            // 如果当前为出诊Tab，自动加载排班
                            if (!isIntroTabSelected) {
                                loadSchedulesForDate(selectedDateIndex);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 更新医生信息到 UI
     */
    private void updateDoctorInfo() {
        if (doctor == null) {
            binding.tvDoctorName.setText(R.string.doctor_detail_title);
            return;
        }

        binding.tvDoctorName.setText(doctor.getDoctorName());

        if (doctor.getTitle() != null && !doctor.getTitle().isEmpty()) {
            binding.tvDoctorTitle.setText(doctor.getTitle());
            binding.tvDoctorTitle.setVisibility(View.VISIBLE);
        } else {
            binding.tvDoctorTitle.setVisibility(View.GONE);
        }

        if (departmentName != null && !departmentName.isEmpty()) {
            binding.tvDoctorDept.setText(departmentName);
            binding.tvDoctorDept.setVisibility(View.VISIBLE);
        } else {
            binding.tvDoctorDept.setVisibility(View.GONE);
        }

        // 简介Tab — 显示完整介绍
        String introduction = doctor.getIntroduction();
        if (!TextUtils.isEmpty(introduction)) {
            binding.tvIntroContent.setText(introduction);
        } else {
            binding.tvIntroContent.setText(R.string.doctor_detail_no_intro);
        }
    }

    // ==================== 6. 排班数据加载 ====================

    /**
     * 加载选中日期下的排班
     */
    private void loadSchedulesForDate(final int dateIndex) {
        if (doctor == null || dateIndex < 0 || dateIndex >= dateList.size()) {
            return;
        }

        final String queryDate = queryDateFormat.format(dateList.get(dateIndex));

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 查询排班
                final List<DoctorSchedule> schedules = dbHelper.getDoctorScheduleDao()
                        .queryActiveSchedulesByDoctor(doctor.getId(), queryDate);

                // 查询每个排班的号源
                final Map<Long, List<RegisterSource>> sourceMap = new HashMap<>();
                if (schedules != null) {
                    for (DoctorSchedule schedule : schedules) {
                        List<RegisterSource> sources = dbHelper.getRegisterSourceDao()
                                .querySourcesBySchedule(schedule.getId());
                        sourceMap.put(schedule.getId(), sources);
                    }
                }

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            displaySchedules(schedules, sourceMap);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 展示排班数据
     */
    private void displaySchedules(List<DoctorSchedule> schedules,
                                   Map<Long, List<RegisterSource>> sourceMap) {
        if (schedules == null || schedules.isEmpty()) {
            binding.rvSchedule.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.rvSchedule.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
            scheduleAdapter.setData(schedules, sourceMap);
        }
    }

    // ==================== 7. 号源点击跳转 ====================

    /**
     * 点击有余号的槽位，跳转到预约页面
     */
    private void navigateToAppointment(DoctorSchedule schedule, RegisterSource source) {
        if (doctor == null) {
            return;
        }
        ensureCanBookAppointment(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(DoctorDetailActivity.this, AppointmentActivity.class);
                intent.putExtra("source_id", source.getId());
                intent.putExtra("doctor_id", doctor.getId());
                intent.putExtra("doctor_name", doctor.getDoctorName());
                intent.putExtra("department_name", departmentName);
                intent.putExtra("schedule_date", schedule.getScheduleDate());
                intent.putExtra("period", schedule.getPeriod());
                intent.putExtra("clinic_room", schedule.getClinicRoom());
                intent.putExtra("slot_time", source.getSlotStartTime() + "-" + source.getSlotEndTime());
                intent.putExtra("register_fee", source.getRegisterFee());
                startActivity(intent);
            }
        });
    }

    private boolean ensureCanBookAppointment(final Runnable onVerified) {
        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return false;
        }
        final long currentUserId = SPUtil.getCurrentUserId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = dbHelper.getUserDao().queryUserById(currentUserId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            if (user != null && user.isRealNameVerified()) {
                                if (onVerified != null) {
                                    onVerified.run();
                                }
                            } else {
                                Intent intent = new Intent(DoctorDetailActivity.this, IDVerificationActivity.class);
                                intent.putExtra("show_not_verified_dialog", true);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }
        }).start();
        return false;
    }

    // ==================== 8. 日期选择器适配器 ====================

    /**
     * 日期选择器 Horizontal RecyclerView 适配器
     */
    private class DatePickerAdapter extends RecyclerView.Adapter<DatePickerAdapter.DateViewHolder> {

        @NonNull
        @Override
        public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(DoctorDetailActivity.this)
                    .inflate(R.layout.item_date_chip, parent, false);
            return new DateViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
            Date date = dateList.get(position);
            String displayText = displayDateFormat.format(date);
            holder.tvDateText.setText(displayText);

            boolean isSelected = (position == selectedDateIndex);
            if (isSelected) {
                holder.tvDateText.setBackgroundResource(R.drawable.shape_date_chip_selected);
                holder.tvDateText.setTextColor(getColor(R.color.white));
            } else {
                holder.tvDateText.setBackgroundResource(R.drawable.shape_date_chip_unselected);
                holder.tvDateText.setTextColor(getColor(R.color.on_surface_variant));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedDateIndex != position) {
                        int previousSelected = selectedDateIndex;
                        selectedDateIndex = position;
                        notifyItemChanged(previousSelected);
                        notifyItemChanged(selectedDateIndex);
                        loadSchedulesForDate(selectedDateIndex);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dateList.size();
        }

        class DateViewHolder extends RecyclerView.ViewHolder {
            TextView tvDateText;

            DateViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDateText = itemView.findViewById(R.id.tv_date_text);
            }
        }
    }
}
