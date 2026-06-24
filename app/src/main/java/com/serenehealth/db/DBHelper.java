package com.serenehealth.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.serenehealth.db.AdminUserDao;
import com.serenehealth.db.AppointmentDao;
import com.serenehealth.db.BannerDao;
import com.serenehealth.db.DepartmentDao;
import com.serenehealth.db.DoctorDao;
import com.serenehealth.db.DoctorScheduleDao;
import com.serenehealth.db.FeedbackDao;
import com.serenehealth.db.HelpContentDao;
import com.serenehealth.db.MedicalCardDao;
import com.serenehealth.db.MedicalCardRecordDao;
import com.serenehealth.db.MessageDao;
import com.serenehealth.db.PaymentOrderDao;
import com.serenehealth.db.RegisterSourceDao;
import com.serenehealth.db.SymptomDepartmentRuleDao;
import com.serenehealth.db.UserDao;
import com.serenehealth.db.VisitHistoryDao;

/**
 * 数据库帮助类，负责管理应用数据库的创建、升级和 DAO 实例的获取。
 * 使用单例模式确保全局只有一个数据库连接实例。
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "serene_health.db";

    private static final int DB_VERSION = 1;

    private static volatile DBHelper instance;

    private UserDao userDao;

    private DepartmentDao departmentDao;

    private DoctorDao doctorDao;

    private DoctorScheduleDao doctorScheduleDao;

    private RegisterSourceDao registerSourceDao;

    private AppointmentDao appointmentDao;

    private PaymentOrderDao paymentOrderDao;

    private MedicalCardDao medicalCardDao;

    private MedicalCardRecordDao medicalCardRecordDao;

    private VisitHistoryDao visitHistoryDao;

    private MessageDao messageDao;

    private BannerDao bannerDao;

    private FeedbackDao feedbackDao;

    private AdminUserDao adminUserDao;

    private SymptomDepartmentRuleDao symptomDepartmentRuleDao;

    private HelpContentDao helpContentDao;

    private DBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. 用户/就诊人表
        db.execSQL("CREATE TABLE t_user (\n"
                + "    id            INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    phone         TEXT    NOT NULL UNIQUE,\n"
                + "    password      TEXT,\n"
                + "    real_name     TEXT    NOT NULL,\n"
                + "    gender        INTEGER,\n"
                + "    birth_date    TEXT,\n"
                + "    id_card_no    TEXT    UNIQUE,\n"
                + "    health_score  INTEGER,\n"
                + "    member_level  TEXT    DEFAULT '普通会员',\n"
                + "    create_time   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time   TEXT    NOT NULL DEFAULT (datetime('now','localtime'))\n"
                + ")");

        // 2. 科室表
        db.execSQL("CREATE TABLE t_department (\n"
                + "    id          INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    dept_name   TEXT    NOT NULL,\n"
                + "    dept_code   TEXT    UNIQUE,\n"
                + "    parent_id   INTEGER,\n"
                + "    description TEXT,\n"
                + "    sort_no     INTEGER DEFAULT 0,\n"
                + "    create_time TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (parent_id) REFERENCES t_department(id)\n"
                + ")");

        // 3. 医生表
        db.execSQL("CREATE TABLE t_doctor (\n"
                + "    id            INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    department_id INTEGER NOT NULL,\n"
                + "    doctor_name   TEXT    NOT NULL,\n"
                + "    gender        INTEGER,\n"
                + "    title         TEXT,\n"
                + "    introduction  TEXT,\n"
                + "    status        INTEGER DEFAULT 1,\n"
                + "    sort_no       INTEGER DEFAULT 0,\n"
                + "    create_time   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (department_id) REFERENCES t_department(id)\n"
                + ")");

        // 4. 医生排班表
        db.execSQL("CREATE TABLE t_doctor_schedule (\n"
                + "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    doctor_id       INTEGER NOT NULL,\n"
                + "    schedule_date   TEXT    NOT NULL,\n"
                + "    period          TEXT    NOT NULL,\n"
                + "    clinic_room     TEXT,\n"
                + "    start_time      TEXT    NOT NULL,\n"
                + "    end_time        TEXT    NOT NULL,\n"
                + "    schedule_status INTEGER DEFAULT 1,\n"
                + "    create_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (doctor_id) REFERENCES t_doctor(id),\n"
                + "    UNIQUE(doctor_id, schedule_date, period)\n"
                + ")");

        // 5. 挂号号源表（20分钟/段）
        db.execSQL("CREATE TABLE t_register_source (\n"
                + "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    schedule_id     INTEGER NOT NULL,\n"
                + "    slot_start_time TEXT    NOT NULL,\n"
                + "    slot_end_time   TEXT    NOT NULL,\n"
                + "    total_num       INTEGER NOT NULL DEFAULT 1,\n"
                + "    remain_num      INTEGER NOT NULL DEFAULT 1,\n"
                + "    register_fee    REAL    DEFAULT 20.00,\n"
                + "    source_status   INTEGER DEFAULT 1,\n"
                + "    version         INTEGER DEFAULT 0,\n"
                + "    create_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (schedule_id) REFERENCES t_doctor_schedule(id),\n"
                + "    UNIQUE(schedule_id, slot_start_time, slot_end_time)\n"
                + ")");

        // 6. 预约挂号记录表
        db.execSQL("CREATE TABLE t_appointment (\n"
                + "    id                 INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    appointment_no     TEXT    NOT NULL UNIQUE,\n"
                + "    user_id            INTEGER NOT NULL,\n"
                + "    source_id          INTEGER NOT NULL,\n"
                + "    appointment_status TEXT    DEFAULT 'BOOKED',\n"
                + "    cancel_reason      TEXT,\n"
                + "    create_time        TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time        TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (user_id)   REFERENCES t_user(id),\n"
                + "    FOREIGN KEY (source_id) REFERENCES t_register_source(id)\n"
                + ")");

        // 7. 缴费订单表
        db.execSQL("CREATE TABLE t_payment_order (\n"
                + "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    order_no        TEXT    NOT NULL UNIQUE,\n"
                + "    appointment_id  INTEGER NOT NULL,\n"
                + "    user_id         INTEGER NOT NULL,\n"
                + "    medical_card_id INTEGER,\n"
                + "    amount          REAL    NOT NULL,\n"
                + "    pay_channel     TEXT    DEFAULT 'MOCK_PAY',\n"
                + "    order_status    TEXT    DEFAULT 'UNPAID',\n"
                + "    pay_time        TEXT,\n"
                + "    create_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (appointment_id)  REFERENCES t_appointment(id),\n"
                + "    FOREIGN KEY (user_id)         REFERENCES t_user(id),\n"
                + "    FOREIGN KEY (medical_card_id) REFERENCES t_medical_card(id)\n"
                + ")");

        // 8. 模拟医保卡表
        db.execSQL("CREATE TABLE t_medical_card (\n"
                + "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    user_id         INTEGER NOT NULL UNIQUE,\n"
                + "    holder_name     TEXT    NOT NULL,\n"
                + "    id_card_no      TEXT    NOT NULL,\n"
                + "    medical_card_no TEXT    NOT NULL UNIQUE,\n"
                + "    bind_phone      TEXT    NOT NULL,\n"
                + "    balance         REAL    DEFAULT 0.00,\n"
                + "    valid_start     TEXT    NOT NULL,\n"
                + "    valid_end       TEXT    NOT NULL,\n"
                + "    bind_status     TEXT    DEFAULT 'BOUND',\n"
                + "    bind_time       TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    unbind_time     TEXT,\n"
                + "    create_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (user_id) REFERENCES t_user(id)\n"
                + ")");

        // 9. 医保消费明细表
        db.execSQL("CREATE TABLE t_medical_card_record (\n"
                + "    id               INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    card_id          INTEGER NOT NULL,\n"
                + "    user_id          INTEGER NOT NULL,\n"
                + "    related_order_id INTEGER,\n"
                + "    record_type      TEXT    NOT NULL,\n"
                + "    amount           REAL    NOT NULL,\n"
                + "    balance_after    REAL,\n"
                + "    description      TEXT,\n"
                + "    record_time      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    create_time      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (card_id)          REFERENCES t_medical_card(id),\n"
                + "    FOREIGN KEY (user_id)          REFERENCES t_user(id),\n"
                + "    FOREIGN KEY (related_order_id) REFERENCES t_payment_order(id)\n"
                + ")");

        // 10. 就诊历史记录表
        db.execSQL("CREATE TABLE t_visit_history (\n"
                + "    id               INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    user_id          INTEGER NOT NULL,\n"
                + "    appointment_id   INTEGER,\n"
                + "    department_id    INTEGER,\n"
                + "    doctor_id        INTEGER,\n"
                + "    visit_time       TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    chief_complaint  TEXT,\n"
                + "    diagnosis        TEXT,\n"
                + "    treatment_advice TEXT,\n"
                + "    remark           TEXT,\n"
                + "    create_time      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (user_id)        REFERENCES t_user(id),\n"
                + "    FOREIGN KEY (appointment_id) REFERENCES t_appointment(id),\n"
                + "    FOREIGN KEY (department_id)  REFERENCES t_department(id),\n"
                + "    FOREIGN KEY (doctor_id)      REFERENCES t_doctor(id)\n"
                + ")");

        // 11. 消息通知表
        db.execSQL("CREATE TABLE t_message (\n"
                + "    id            INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    user_id       INTEGER NOT NULL,\n"
                + "    title         TEXT    NOT NULL,\n"
                + "    content       TEXT    NOT NULL,\n"
                + "    business_type TEXT,\n"
                + "    business_id   INTEGER,\n"
                + "    is_read       INTEGER DEFAULT 0,\n"
                + "    send_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    create_time   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (user_id) REFERENCES t_user(id)\n"
                + ")");

        // 12. 首页轮播图表
        db.execSQL("CREATE TABLE t_banner (\n"
                + "    id         INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    title      TEXT,\n"
                + "    image_url  TEXT    NOT NULL,\n"
                + "    jump_type  TEXT    DEFAULT 'NONE',\n"
                + "    jump_value TEXT,\n"
                + "    sort_no    INTEGER DEFAULT 0,\n"
                + "    status     INTEGER DEFAULT 1,\n"
                + "    start_time TEXT,\n"
                + "    end_time   TEXT,\n"
                + "    create_time TEXT   NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time TEXT   NOT NULL DEFAULT (datetime('now','localtime'))\n"
                + ")");

        // 13. 满意度评价表
        db.execSQL("CREATE TABLE t_feedback (\n"
                + "    id             INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    user_id        INTEGER NOT NULL,\n"
                + "    appointment_id INTEGER,\n"
                + "    doctor_id      INTEGER,\n"
                + "    doctor_score   INTEGER,\n"
                + "    service_score  INTEGER,\n"
                + "    visit_score    INTEGER,\n"
                + "    content        TEXT,\n"
                + "    create_time    TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (user_id)        REFERENCES t_user(id),\n"
                + "    FOREIGN KEY (appointment_id) REFERENCES t_appointment(id),\n"
                + "    FOREIGN KEY (doctor_id)      REFERENCES t_doctor(id)\n"
                + ")");

        // 14. 后台账号表
        db.execSQL("CREATE TABLE t_admin_user (\n"
                + "    id              INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    username        TEXT    NOT NULL UNIQUE,\n"
                + "    password        TEXT    NOT NULL,\n"
                + "    real_name       TEXT    NOT NULL,\n"
                + "    role_type       TEXT    NOT NULL,\n"
                + "    doctor_id       INTEGER,\n"
                + "    phone           TEXT,\n"
                + "    status          INTEGER DEFAULT 1,\n"
                + "    last_login_time TEXT,\n"
                + "    create_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (doctor_id) REFERENCES t_doctor(id)\n"
                + ")");

        // 15. 症状-科室匹配规则表
        db.execSQL("CREATE TABLE t_symptom_department_rule (\n"
                + "    id               INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    symptom_keyword  TEXT    NOT NULL,\n"
                + "    department_id    INTEGER NOT NULL,\n"
                + "    recommend_reason TEXT,\n"
                + "    sort_no          INTEGER DEFAULT 0,\n"
                + "    status           INTEGER DEFAULT 1,\n"
                + "    create_time      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    FOREIGN KEY (department_id) REFERENCES t_department(id)\n"
                + ")");

        // 16. 帮助/隐私/流程内容表（选做）
        db.execSQL("CREATE TABLE t_help_content (\n"
                + "    id           INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    content_type TEXT    NOT NULL,\n"
                + "    title        TEXT    NOT NULL,\n"
                + "    content      TEXT,\n"
                + "    video_url    TEXT,\n"
                + "    sort_no      INTEGER DEFAULT 0,\n"
                + "    status       INTEGER DEFAULT 1,\n"
                + "    create_time  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),\n"
                + "    update_time  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))\n"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 按从表到主表的顺序删除，避免外键冲突
        db.execSQL("DROP TABLE IF EXISTS t_help_content");
        db.execSQL("DROP TABLE IF EXISTS t_symptom_department_rule");
        db.execSQL("DROP TABLE IF EXISTS t_admin_user");
        db.execSQL("DROP TABLE IF EXISTS t_feedback");
        db.execSQL("DROP TABLE IF EXISTS t_banner");
        db.execSQL("DROP TABLE IF EXISTS t_message");
        db.execSQL("DROP TABLE IF EXISTS t_visit_history");
        db.execSQL("DROP TABLE IF EXISTS t_medical_card_record");
        db.execSQL("DROP TABLE IF EXISTS t_medical_card");
        db.execSQL("DROP TABLE IF EXISTS t_payment_order");
        db.execSQL("DROP TABLE IF EXISTS t_appointment");
        db.execSQL("DROP TABLE IF EXISTS t_register_source");
        db.execSQL("DROP TABLE IF EXISTS t_doctor_schedule");
        db.execSQL("DROP TABLE IF EXISTS t_doctor");
        db.execSQL("DROP TABLE IF EXISTS t_department");
        db.execSQL("DROP TABLE IF EXISTS t_user");
        onCreate(db);
    }

    // ==================== DAO getters ====================

    public UserDao getUserDao() {
        // TODO 由 UserDao 实现补充
        if (userDao == null) {
            userDao = new UserDao(this);
        }
        return userDao;
    }

    public DepartmentDao getDepartmentDao() {
        // TODO 由 DepartmentDao 实现补充
        if (departmentDao == null) {
            departmentDao = new DepartmentDao(this);
        }
        return departmentDao;
    }

    public DoctorDao getDoctorDao() {
        // TODO 由 DoctorDao 实现补充
        if (doctorDao == null) {
            doctorDao = new DoctorDao(this);
        }
        return doctorDao;
    }

    public DoctorScheduleDao getDoctorScheduleDao() {
        // TODO 由 DoctorScheduleDao 实现补充
        if (doctorScheduleDao == null) {
            doctorScheduleDao = new DoctorScheduleDao(this);
        }
        return doctorScheduleDao;
    }

    public RegisterSourceDao getRegisterSourceDao() {
        // TODO 由 RegisterSourceDao 实现补充
        if (registerSourceDao == null) {
            registerSourceDao = new RegisterSourceDao(this);
        }
        return registerSourceDao;
    }

    public AppointmentDao getAppointmentDao() {
        // TODO 由 AppointmentDao 实现补充
        if (appointmentDao == null) {
            appointmentDao = new AppointmentDao(this);
        }
        return appointmentDao;
    }

    public PaymentOrderDao getPaymentOrderDao() {
        // TODO 由 PaymentOrderDao 实现补充
        if (paymentOrderDao == null) {
            paymentOrderDao = new PaymentOrderDao(this);
        }
        return paymentOrderDao;
    }

    public MedicalCardDao getMedicalCardDao() {
        // TODO 由 MedicalCardDao 实现补充
        if (medicalCardDao == null) {
            medicalCardDao = new MedicalCardDao(this);
        }
        return medicalCardDao;
    }

    public MedicalCardRecordDao getMedicalCardRecordDao() {
        // TODO 由 MedicalCardRecordDao 实现补充
        if (medicalCardRecordDao == null) {
            medicalCardRecordDao = new MedicalCardRecordDao(this);
        }
        return medicalCardRecordDao;
    }

    public VisitHistoryDao getVisitHistoryDao() {
        // TODO 由 VisitHistoryDao 实现补充
        if (visitHistoryDao == null) {
            visitHistoryDao = new VisitHistoryDao(this);
        }
        return visitHistoryDao;
    }

    public MessageDao getMessageDao() {
        // TODO 由 MessageDao 实现补充
        if (messageDao == null) {
            messageDao = new MessageDao(this);
        }
        return messageDao;
    }

    public BannerDao getBannerDao() {
        // TODO 由 BannerDao 实现补充
        if (bannerDao == null) {
            bannerDao = new BannerDao(this);
        }
        return bannerDao;
    }

    public FeedbackDao getFeedbackDao() {
        // TODO 由 FeedbackDao 实现补充
        if (feedbackDao == null) {
            feedbackDao = new FeedbackDao(this);
        }
        return feedbackDao;
    }

    public AdminUserDao getAdminUserDao() {
        // TODO 由 AdminUserDao 实现补充
        if (adminUserDao == null) {
            adminUserDao = new AdminUserDao(this);
        }
        return adminUserDao;
    }

    public SymptomDepartmentRuleDao getSymptomDepartmentRuleDao() {
        // TODO 由 SymptomDepartmentRuleDao 实现补充
        if (symptomDepartmentRuleDao == null) {
            symptomDepartmentRuleDao = new SymptomDepartmentRuleDao(this);
        }
        return symptomDepartmentRuleDao;
    }

    public HelpContentDao getHelpContentDao() {
        // TODO 由 HelpContentDao 实现补充
        if (helpContentDao == null) {
            helpContentDao = new HelpContentDao(this);
        }
        return helpContentDao;
    }
}
