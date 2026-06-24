package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.R;
import com.serenehealth.bean.Appointment;
import com.serenehealth.bean.RegisterSource;
import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityAppointmentBinding;
import com.serenehealth.db.AppointmentDao;
import com.serenehealth.db.DBHelper;
import com.serenehealth.db.RegisterSourceDao;
import com.serenehealth.db.UserDao;
import com.serenehealth.util.SPUtil;

/**
 * 预约挂号页面（功能4）
 * 从 DoctorDetailActivity 接收号源信息，完成预约创建
 */
public class AppointmentActivity extends AppCompatActivity {

    // ==================== 1. ViewBinding ====================
    private ActivityAppointmentBinding binding;

    // ==================== 2. 成员变量 ====================
    private DBHelper dbHelper;
    private AppointmentDao appointmentDao;
    private UserDao userDao;
    private RegisterSourceDao registerSourceDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Intent 传入的数据
    private long sourceId;
    private long doctorId;
    private String doctorName;
    private String departmentName;
    private String scheduleDate;
    private String period;
    private String clinicRoom;
    private String slotTime;
    private double registerFee;

    // 当前用户
    private long currentUserId;

    // 是否正在处理预约（防重复提交）
    private boolean isProcessing = false;

    // ==================== 3. 生命周期 ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DBHelper.getInstance(this);
        appointmentDao = dbHelper.getAppointmentDao();
        userDao = dbHelper.getUserDao();
        registerSourceDao = dbHelper.getRegisterSourceDao();

        getIntentData();
        initViews();
        loadPatientInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // ==================== 4. 获取 Intent 数据 ====================
    private void getIntentData() {
        sourceId = getIntent().getLongExtra("source_id", -1);
        doctorId = getIntent().getLongExtra("doctor_id", -1);
        doctorName = getIntent().getStringExtra("doctor_name");
        departmentName = getIntent().getStringExtra("department_name");
        scheduleDate = getIntent().getStringExtra("schedule_date");
        period = getIntent().getStringExtra("period");
        clinicRoom = getIntent().getStringExtra("clinic_room");
        slotTime = getIntent().getStringExtra("slot_time");
        registerFee = getIntent().getDoubleExtra("register_fee", 0);
        currentUserId = SPUtil.getCurrentUserId();
    }

    // ==================== 5. 初始化视图 ====================
    private void initViews() {
        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 填充预约信息
        binding.tvDeptValue.setText(departmentName != null ? departmentName : "");
        binding.tvDoctorValue.setText(doctorName != null ? doctorName : "");
        binding.tvDateValue.setText(scheduleDate != null ? scheduleDate : "");
        binding.tvPeriodValue.setText((period != null ? period : "") + " (" + (slotTime != null ? slotTime : "") + ")");
        binding.tvClinicValue.setText(clinicRoom != null ? clinicRoom : "");
        binding.tvFeeValue.setText(String.format("¥%.2f", registerFee));

        // 确认预约按钮
        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAppointment();
            }
        });

        // 返回首页按钮
        binding.btnReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回到 MainActivity
                Intent intent = new Intent(AppointmentActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    // ==================== 6. 加载就诊人信息 ====================
    private void loadPatientInfo() {
        if (currentUserId <= 0) {
            binding.tvPatientName.setText(R.string.appointment_title);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final User user = userDao.queryUserById(currentUserId);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            displayPatientInfo(user);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 展示就诊人信息
     */
    private void displayPatientInfo(User user) {
        if (user == null) {
            binding.tvPatientName.setText(R.string.appointment_title);
            return;
        }

        // 姓名
        binding.tvPatientName.setText(user.getRealName() != null ? user.getRealName() : "");

        // 性别标签
        String genderText;
        if (user.getGender() == 1) {
            genderText = getString(R.string.appointment_gender_male);
        } else if (user.getGender() == 2) {
            genderText = getString(R.string.appointment_gender_female);
        } else {
            genderText = "";
        }
        binding.tvPatientGender.setText(genderText);
        binding.tvPatientGender.setVisibility(genderText.isEmpty() ? View.GONE : View.VISIBLE);

        // 身份证号（脱敏）
        String idCard = user.getIdCardNo();
        if (idCard != null && !idCard.isEmpty()) {
            binding.tvPatientIdcard.setText(maskIdCard(idCard));
            binding.tvPatientIdcard.setVisibility(View.VISIBLE);
        } else {
            binding.tvPatientIdcard.setVisibility(View.GONE);
        }

        // 手机号
        String phone = user.getPhone();
        if (phone != null && !phone.isEmpty()) {
            binding.tvPatientPhone.setText(phone);
            binding.tvPatientPhone.setVisibility(View.VISIBLE);
        } else {
            binding.tvPatientPhone.setVisibility(View.GONE);
        }
    }

    // ==================== 7. 预约流程 ====================

    /**
     * 执行预约操作
     */
    private void performAppointment() {
        if (isProcessing) {
            return;
        }

        if (currentUserId <= 0) {
            Toast.makeText(this, R.string.appointment_fail_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        // 按钮变灰，防止重复提交
        isProcessing = true;
        binding.btnConfirm.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // a. 构造 Appointment 对象
                Appointment appointment = new Appointment();
                appointment.setUserId(currentUserId);
                appointment.setSourceId(sourceId);
                appointment.setAppointmentStatus("BOOKED");
                // 生成预约编号
                appointment.setAppointmentNo(generateAppointmentNo());

                // b. 调用 DAO 创建预约
                final long appointmentId = appointmentDao.createAppointment(appointment);

                if (appointmentId != -1) {
                    // 成功
                    // c. 查询号源获取挂号费
                    final RegisterSource source = registerSourceDao.querySourceById(sourceId);
                    final double fee = (source != null) ? source.getRegisterFee() : registerFee;

                    // 查询预约详情获取完整信息
                    final Appointment created = appointmentDao.queryAppointmentDetail(appointmentId);
                    final String appointmentNo = (created != null && created.getAppointmentNo() != null)
                            ? created.getAppointmentNo() : appointment.getAppointmentNo();

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                showSuccessPage(appointmentNo, fee);
                            }
                        }
                    });
                } else {
                    // 失败
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                Toast.makeText(AppointmentActivity.this,
                                        R.string.appointment_fail_msg, Toast.LENGTH_SHORT).show();
                                // 恢复按钮
                                isProcessing = false;
                                binding.btnConfirm.setEnabled(true);
                                binding.btnConfirm.setText(R.string.appointment_confirm);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 显示成功页面
     */
    private void showSuccessPage(String appointmentNo, double amount) {
        // 隐藏表单区域
        binding.cardInfo.setVisibility(View.GONE);
        binding.cardPatient.setVisibility(View.GONE);
        binding.btnConfirm.setVisibility(View.GONE);

        // 设置成功信息
        binding.tvAppointmentNo.setText(getString(R.string.appointment_no_prefix) + appointmentNo);
        binding.tvPayAmount.setText(getString(R.string.appointment_amount_prefix)
                + String.format("¥%.2f", amount));

        // 显示成功页面
        binding.successContainer.setVisibility(View.VISIBLE);

        isProcessing = false;
    }

    // ==================== 8. 工具方法 ====================

    /**
     * 生成预约编号
     */
    private String generateAppointmentNo() {
        return "APPT" + System.currentTimeMillis();
    }

    /**
     * 身份证脱敏：前3位 + ****** + 后2位
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 6) {
            return idCard;
        }
        return idCard.substring(0, 3) + "******" + idCard.substring(idCard.length() - 2);
    }
}
