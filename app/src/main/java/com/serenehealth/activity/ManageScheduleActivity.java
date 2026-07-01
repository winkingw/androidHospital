package com.serenehealth.activity;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.serenehealth.R;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.DoctorSchedule;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.databinding.ActivityManageScheduleBinding;
import com.serenehealth.databinding.DialogEditSourceBinding;
import com.serenehealth.databinding.ItemScheduleCardBinding;
import com.serenehealth.db.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageScheduleActivity extends AppCompatActivity {

    private ActivityManageScheduleBinding binding;
    private DBHelper dbHelper;

    private List<Department> departmentList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<DoctorSchedule> scheduleList = new ArrayList<>();
    private ScheduleAdapter adapter;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DBHelper.getInstance(this);

        setupToolbar();
        setupSpinners();
        setupRecyclerView();
        setupGenerateButton();
    }

    // ==================== Toolbar ====================

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    // ==================== Spinners ====================

    private void setupSpinners() {
        departmentList = dbHelper.getDepartmentDao().queryAllDepartments();
        List<String> deptNames = new ArrayList<>();
        deptNames.add("请选择科室");
        for (Department d : departmentList) {
            deptNames.add(d.getDeptName());
        }
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deptNames);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDepartment.setAdapter(deptAdapter);

        // Initialize doctor spinner with placeholder
        List<String> emptyDoctorNames = new ArrayList<>();
        emptyDoctorNames.add("请选择医生");
        ArrayAdapter<String> emptyDoctorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emptyDoctorNames);
        emptyDoctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDoctor.setAdapter(emptyDoctorAdapter);

        binding.spDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    doctorList.clear();
                    resetDoctorSpinner();
                    scheduleList.clear();
                    notifyScheduleChanged();
                    updateEmptyState();
                    return;
                }
                Department dept = departmentList.get(position - 1);
                doctorList = dbHelper.getDoctorDao().queryDoctorsByDepartment(dept.getId());
                List<String> doctorNames = new ArrayList<>();
                doctorNames.add("请选择医生");
                for (Doctor doc : doctorList) {
                    doctorNames.add(doc.getDoctorName());
                }
                ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(ManageScheduleActivity.this,
                        android.R.layout.simple_spinner_item, doctorNames);
                doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spDoctor.setAdapter(doctorAdapter);
                // Reset schedule list when department changes
                scheduleList.clear();
                notifyScheduleChanged();
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.spDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    scheduleList.clear();
                    notifyScheduleChanged();
                    updateEmptyState();
                    return;
                }
                Doctor doctor = doctorList.get(position - 1);
                loadSchedules(doctor.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void resetDoctorSpinner() {
        List<String> doctorNames = new ArrayList<>();
        doctorNames.add("请选择医生");
        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, doctorNames);
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDoctor.setAdapter(doctorAdapter);
    }

    // ==================== Load Schedules ====================

    private void loadSchedules(long doctorId) {
        scheduleList.clear();
        // Query schedules for the next 7 days (matching the generation range)
        for (int day = 1; day <= 7; day++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, day);
            String date = dateFmt.format(cal.getTime());
            List<DoctorSchedule> daySchedules = dbHelper.getDoctorScheduleDao()
                    .querySchedulesByDoctor(doctorId, date);
            scheduleList.addAll(daySchedules);
        }
        notifyScheduleChanged();
        updateEmptyState();
    }

    // ==================== RecyclerView ====================

    private void setupRecyclerView() {
        binding.rvScheduleList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter();
        binding.rvScheduleList.setAdapter(adapter);
    }

    private void notifyScheduleChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateEmptyState() {
        if (scheduleList.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvScheduleList.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvScheduleList.setVisibility(View.VISIBLE);
        }
    }

    // ==================== Generate Button ====================

    private void setupGenerateButton() {
        binding.btnGenerate.setOnClickListener(v -> {
            if (binding.spDepartment.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "请先选择科室", Toast.LENGTH_SHORT).show();
                return;
            }
            if (binding.spDoctor.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "请先选择医生", Toast.LENGTH_SHORT).show();
                return;
            }

            Doctor doctor = doctorList.get(binding.spDoctor.getSelectedItemPosition() - 1);
            long doctorId = doctor.getId();

            // Check if schedules already exist for the next 7 days
            boolean hasExisting = false;
            for (int day = 1; day <= 7; day++) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, day);
                String date = dateFmt.format(cal.getTime());
                List<DoctorSchedule> existing = dbHelper.getDoctorScheduleDao()
                        .querySchedulesByDoctor(doctorId, date);
                if (!existing.isEmpty()) {
                    hasExisting = true;
                    break;
                }
            }

            if (hasExisting) {
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("该医生在未来7天内已有排班，是否重新生成？重新生成将先取消现有排班。")
                        .setPositiveButton("重新生成", (dialog, which) -> {
                            cancelExistingAndRegenerate(doctorId);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                generateSchedules(doctorId);
            }
        });
    }

    private void cancelExistingAndRegenerate(long doctorId) {
        String clinicRoom = resolveDefaultClinicRoom(doctorId);
        // Delete existing schedules for the next 7 days (physically remove rows
        // so the UNIQUE(doctor_id, schedule_date, period) constraint won't block
        // the subsequent insert).
        for (int day = 1; day <= 7; day++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, day);
            String date = dateFmt.format(cal.getTime());
            List<DoctorSchedule> existing = dbHelper.getDoctorScheduleDao()
                    .querySchedulesByDoctor(doctorId, date);
            for (DoctorSchedule s : existing) {
                dbHelper.getDoctorScheduleDao().deleteSchedule(s.getId());
            }
        }
        generateSchedules(doctorId, clinicRoom);
    }

    private void generateSchedules(long doctorId) {
        generateSchedules(doctorId, resolveDefaultClinicRoom(doctorId));
    }

    private void generateSchedules(long doctorId, String clinicRoom) {
        int count = 0;
        for (int day = 1; day <= 7; day++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, day);
            String date = dateFmt.format(cal.getTime());

            // Morning schedule: 08:00 - 12:00
            DoctorSchedule morning = new DoctorSchedule();
            morning.setDoctorId(doctorId);
            morning.setScheduleDate(date);
            morning.setPeriod("MORNING");
            morning.setStartTime("08:00:00");
            morning.setEndTime("12:00:00");
            morning.setClinicRoom(clinicRoom);
            morning.setScheduleStatus(1);
            long morningId = dbHelper.getDoctorScheduleDao().insertSchedule(morning);
            if (morningId != -1) {
                dbHelper.getRegisterSourceDao().generateSlots(morningId, "08:00:00", "12:00:00");
                count++;
            }

            // Afternoon schedule: 13:00 - 17:00
            DoctorSchedule afternoon = new DoctorSchedule();
            afternoon.setDoctorId(doctorId);
            afternoon.setScheduleDate(date);
            afternoon.setPeriod("AFTERNOON");
            afternoon.setStartTime("13:00:00");
            afternoon.setEndTime("17:00:00");
            afternoon.setClinicRoom(clinicRoom);
            afternoon.setScheduleStatus(1);
            long afternoonId = dbHelper.getDoctorScheduleDao().insertSchedule(afternoon);
            if (afternoonId != -1) {
                dbHelper.getRegisterSourceDao().generateSlots(afternoonId, "13:00:00", "17:00:00");
                count++;
            }
        }

        Toast.makeText(this, "已生成 " + count + " 条排班记录", Toast.LENGTH_SHORT).show();
        // Refresh the schedule list
        loadSchedules(doctorId);
    }

    private String resolveDefaultClinicRoom(long doctorId) {
        String clinicRoom = dbHelper.getDoctorScheduleDao().queryLatestClinicRoom(doctorId);
        if (clinicRoom != null && !clinicRoom.trim().isEmpty()) {
            return clinicRoom;
        }
        return "B栋" + ((doctorId % 5) + 1) + "楼 诊室" + (doctorId % 10 + 1);
    }

    // ==================== Schedule Slots Dialog ====================

    private void showScheduleSlotsDialog(DoctorSchedule schedule) {
        List<RegisterSource> sources = dbHelper.getRegisterSourceDao()
                .querySourcesBySchedule(schedule.getId());
        if (sources.isEmpty()) {
            Toast.makeText(this, "暂无号源信息", Toast.LENGTH_SHORT).show();
            return;
        }

        String periodText = "MORNING".equals(schedule.getPeriod()) ? "上午" : "下午";

        // Build a vertical LinearLayout to hold source cards
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int containerPad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(containerPad, containerPad, containerPad, 0);

        for (RegisterSource src : sources) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            card.setLayoutParams(cardLp);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            card.setRadius((int) (8 * getResources().getDisplayMetrics().density));
            card.setCardElevation(0);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.outline_variant));
            card.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            card.setContentPadding(
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density));
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(v -> showEditSourceDialog(src));

            // Build text content: time (bold) + remain/total + fee
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            String timeStr = src.getSlotStartTime() + " - " + src.getSlotEndTime();
            ssb.append(timeStr);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, timeStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append("\n余: ").append(String.valueOf(src.getRemainNum()))
                    .append("/").append(String.valueOf(src.getTotalNum()))
                    .append("    ¥").append(String.format("%.2f", src.getRegisterFee()));

            TextView tv = new TextView(this);
            tv.setText(ssb);
            tv.setTextSize(14);
            tv.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            tv.setLineSpacing(0, 1.2f);

            if (src.getSourceStatus() == 0) {
                tv.setAlpha(0.5f);
                tv.setPaintFlags(tv.getPaintFlags()
                        | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }

            card.addView(tv);
            container.addView(card);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container);

        new AlertDialog.Builder(this)
                .setTitle(schedule.getScheduleDate() + " " + periodText + " 号源详情（点击编辑）")
                .setView(scrollView)
                .setPositiveButton("关闭", null)
                .show();
    }

    // ==================== Edit Source Dialog ====================

    private void showEditSourceDialog(RegisterSource src) {
        DialogEditSourceBinding dialogBinding = DialogEditSourceBinding.inflate(
                LayoutInflater.from(this));

        // Pre-fill fields from the RegisterSource object
        dialogBinding.etRemain.setText(String.valueOf(src.getRemainNum()));
        dialogBinding.etTotal.setText(String.valueOf(src.getTotalNum()));
        dialogBinding.etFee.setText(String.format("%.2f", src.getRegisterFee()));
        dialogBinding.cbStatus.setChecked(src.getSourceStatus() == 1);

        String title = src.getSlotStartTime() + " - " + src.getSlotEndTime();

        new AlertDialog.Builder(this)
                .setTitle("编辑号源: " + title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("保存", (dialog, which) -> {
                    try {
                        int remainNum = Integer.parseInt(
                                dialogBinding.etRemain.getText().toString().trim());
                        int totalNum = Integer.parseInt(
                                dialogBinding.etTotal.getText().toString().trim());
                        double fee = Double.parseDouble(
                                dialogBinding.etFee.getText().toString().trim());
                        if (remainNum < 0 || totalNum < 0 || remainNum > totalNum || fee < 0) {
                            Toast.makeText(this, "号数设置不合理", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        src.setRemainNum(remainNum);
                        src.setTotalNum(totalNum);
                        src.setRegisterFee(fee);
                        src.setSourceStatus(dialogBinding.cbStatus.isChecked() ? 1 : 0);
                        dbHelper.getRegisterSourceDao().updateSource(src);
                        Toast.makeText(this, "号源已更新", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== Cancel Schedule Dialog ====================

    private void showCancelScheduleDialog(DoctorSchedule schedule) {
        if (schedule.getScheduleStatus() == 0) {
            showRestoreScheduleDialog(schedule);
            return;
        }

        String periodText = "MORNING".equals(schedule.getPeriod()) ? "上午" : "下午";
        new AlertDialog.Builder(this)
                .setTitle("确认停诊")
                .setMessage("确定要停诊 " + schedule.getScheduleDate() + " " + periodText + " 的排班吗？")
                .setPositiveButton("确认停诊", (dialog, which) -> {
                    dbHelper.getDoctorScheduleDao().cancelSchedule(schedule.getId());
                    Toast.makeText(this, "已停诊", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    Doctor doctor = doctorList.get(binding.spDoctor.getSelectedItemPosition() - 1);
                    loadSchedules(doctor.getId());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRestoreScheduleDialog(DoctorSchedule schedule) {
        String periodText = "MORNING".equals(schedule.getPeriod()) ? "上午" : "下午";
        new AlertDialog.Builder(this)
                .setTitle("恢复出诊")
                .setMessage("确定要恢复 " + schedule.getScheduleDate() + " " + periodText + " 的排班吗？")
                .setPositiveButton("确认恢复", (dialog, which) -> {
                    dbHelper.getDoctorScheduleDao().restoreSchedule(schedule.getId());
                    Toast.makeText(this, "已恢复出诊", Toast.LENGTH_SHORT).show();
                    Doctor doctor = doctorList.get(binding.spDoctor.getSelectedItemPosition() - 1);
                    loadSchedules(doctor.getId());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ==================== Schedule Adapter ====================

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemScheduleCardBinding itemBinding = ItemScheduleCardBinding.inflate(
                    LayoutInflater.from(ManageScheduleActivity.this), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DoctorSchedule schedule = scheduleList.get(position);

            // Date
            holder.binding.tvDate.setText(schedule.getScheduleDate());

            // Period
            String periodText = "MORNING".equals(schedule.getPeriod()) ? "上午" : "下午";
            holder.binding.tvPeriod.setText(periodText);

            // Time range
            holder.binding.tvTimeRange.setText(schedule.getStartTime() + " - " + schedule.getEndTime());

            // Status chip
            boolean isNormal = schedule.getScheduleStatus() == 1;
            holder.binding.tvStatus.setText(isNormal ? "正常" : "已停诊");
            int statusColor = ContextCompat.getColor(ManageScheduleActivity.this,
                    isNormal ? R.color.status_visited : R.color.badge_red);
            holder.binding.tvStatus.setTextColor(statusColor);

            // Hint
            holder.binding.tvHint.setText("点击查看号源 >");
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemScheduleCardBinding binding;

            ViewHolder(ItemScheduleCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    DoctorSchedule schedule = scheduleList.get(pos);
                    showScheduleSlotsDialog(schedule);
                });

                itemView.setOnLongClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return true;
                    DoctorSchedule schedule = scheduleList.get(pos);
                    showCancelScheduleDialog(schedule);
                    return true;
                });
            }
        }
    }
}
