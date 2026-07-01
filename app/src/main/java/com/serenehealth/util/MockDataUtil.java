package com.serenehealth.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.db.DBHelper;
import com.serenehealth.db.RegisterSourceDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MockDataUtil {

    private static final Random RANDOM = new Random(42);

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat DATE_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static void initAll(DBHelper dbHelper) {
        insertDepartments(dbHelper);
        insertDoctors(dbHelper);
        insertUsers(dbHelper);
        generateSchedulesAndSources(dbHelper);
        insertAppointments(dbHelper);
        // 医保卡先于缴费单插入，确保外键可用
        insertMedicalCards(dbHelper);
        insertPaymentOrders(dbHelper);
        insertMedicalCardRecords(dbHelper);
        insertVisitHistories(dbHelper);
        insertMessages(dbHelper);
        insertBanners(dbHelper);
        insertFeedbacks(dbHelper);
        insertAdminUsers(dbHelper);
        insertHelpContent(dbHelper);
        insertSymptomRules(dbHelper);
    }

    // ==================== 1. 科室 ====================

    private static void insertDepartments(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[][] data = {
                {"心血管内科", "cardiology", null,        "诊治高血压、冠心病、心律失常、心力衰竭等"},
                {"呼吸内科",   "pulmonology", null,       "诊治哮喘、慢阻肺、肺炎、肺癌等"},
                {"消化内科",   "gastroenterology", null,  "诊治胃炎、消化性溃疡、肝病、胰腺炎等"},
                {"神经内科",   "neurology", null,         "诊治脑卒中、癫痫、帕金森、头痛眩晕等"},
                {"骨科",       "orthopedics", null,        "诊治骨折、关节炎、脊柱疾病、运动损伤等"},
                {"普通外科",   "general_surgery", null,   "胆囊切除、阑尾切除、疝气修补等普外手术"},
                {"妇产科",     "obstetrics", null,         "产检、分娩、妇科炎症、子宫肌瘤等"},
                {"儿科",       "pediatrics", null,         "儿童常见病、生长发育评估、疫苗接种咨询"},
                {"皮肤科",     "dermatology", null,        "湿疹、痤疮、银屑病、皮肤过敏等"},
                {"眼科",       "ophthalmology", null,      "视力检查、白内障、青光眼、眼底病等"},
                {"口腔科",     "stomatology", null,        "补牙、拔牙、正畸、牙周治疗等"},
                {"内分泌科",   "endocrinology", null,      "糖尿病、甲状腺疾病、痛风、骨质疏松等"},
        };
        for (int i = 0; i < data.length; i++) {
            ContentValues values = new ContentValues();
            values.put("dept_name", data[i][0]);
            values.put("dept_code", data[i][1]);
            values.put("sort_no", i + 1);
            values.put("description", data[i][3]);
            db.insert("t_department", null, values);
        }
    }

    // ==================== 2. 医生 ====================

    private static void insertDoctors(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[][] data = {
                {"1",  "张建国", "1", "主任医师",   "医学博士，擅长冠心病介入治疗、高血压管理"},
                {"1",  "李敏",   "2", "副主任医师", "医学硕士，擅长心律失常射频消融、心衰诊疗"},
                {"2",  "王志强", "1", "主任医师",   "医学博士，擅长慢阻肺规范化治疗、支气管镜"},
                {"2",  "陈婷",   "2", "主治医师",   "医学硕士，擅长哮喘个体化治疗、戒烟指导"},
                {"3",  "刘国栋", "1", "主任医师",   "医学博士，擅长消化内镜下诊治、早期胃癌筛查"},
                {"3",  "赵丽华", "2", "副主任医师", "医学硕士，擅长肝病诊疗、炎症性肠病管理"},
                {"4",  "孙文博", "1", "主任医师",   "医学博士，擅长脑血管病介入治疗、头痛鉴别"},
                {"4",  "周晓云", "2", "主治医师",   "医学硕士，擅长癫痫诊疗、神经电生理"},
                {"5",  "郑志刚", "1", "主任医师",   "医学博士，擅长关节置换、脊柱微创手术"},
                {"5",  "马海燕", "2", "副主任医师", "医学硕士，擅长运动医学、关节镜手术"},
                {"6",  "何向荣", "1", "主任医师",   "医学博士，擅长肝胆外科、腹腔镜微创手术"},
                {"6",  "杨丹",   "2", "主治医师",   "医学硕士，擅长甲状腺乳腺外科"},
                {"7",  "陈丽华", "2", "主任医师",   "医学博士，擅长高危妊娠管理、妇科肿瘤"},
                {"7",  "黄美玲", "2", "副主任医师", "医学硕士，擅长产科超声诊断、产后康复"},
                {"8",  "吴国强", "1", "主任医师",   "医学博士，擅长小儿呼吸、儿童生长发育"},
                {"8",  "沈晓芳", "2", "主治医师",   "医学硕士，擅长新生儿疾病、儿童保健"},
                {"9",  "林文峰", "1", "主任医师",   "医学博士，擅长过敏性皮肤病、皮肤激光美容"},
                {"10", "张明华", "1", "主任医师",   "医学博士，擅长白内障超声乳化、屈光手术"},
                {"11", "徐雅琴", "2", "副主任医师", "医学硕士，擅长口腔正畸、种植牙修复"},
                {"12", "许涛",   "1", "主任医师",   "医学博士，擅长糖尿病综合管理、甲状腺穿刺"},
        };
        for (String[] row : data) {
            if (doctorExists(db, Long.parseLong(row[0]), row[1], row[3])) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put("department_id", Long.parseLong(row[0]));
            values.put("doctor_name", row[1]);
            values.put("gender", Integer.parseInt(row[2]));
            values.put("title", row[3]);
            values.put("introduction", row[4]);
            values.put("status", 1);
            db.insert("t_doctor", null, values);
        }
    }

    private static boolean doctorExists(SQLiteDatabase db, long departmentId, String doctorName, String title) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM t_doctor WHERE department_id = ? AND doctor_name = ? AND title = ? LIMIT 1",
                new String[]{String.valueOf(departmentId), doctorName, title});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // ==================== 3. 用户 ====================

    private static void insertUsers(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[][] data = {
                {"13800001111", "123456", "张三", "1", "1990-01-01", "320102199001011234", "98", "黄金会员"},
                {"13800002222", "123456", "李四", "1", "1992-05-05", "320102199205052345", "85", "普通会员"},
                {"13800003333", "123456", "王五", "2", "1988-03-03", "320102198803033456", "72", "普通会员"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("phone", row[0]);
            values.put("password", row[1]);
            values.put("real_name", row[2]);
            values.put("gender", Integer.parseInt(row[3]));
            values.put("birth_date", row[4]);
            values.put("id_card_no", row[5]);
            values.put("real_name_verified", 1);
            values.put("health_score", Integer.parseInt(row[6]));
            values.put("member_level", row[7]);
            db.insert("t_user", null, values);
        }
    }

    // ==================== 4. 排班 + 号源 ====================

    private static void generateSchedulesAndSources(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        RegisterSourceDao sourceDao = dbHelper.getRegisterSourceDao();

        List<Long> activeDoctorIds = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT id FROM t_doctor WHERE status = 1", null);
        try {
            while (c.moveToNext()) {
                activeDoctorIds.add(c.getLong(0));
            }
        } finally {
            c.close();
        }

        for (long doctorId : activeDoctorIds) {
            for (int day = 1; day <= 7; day++) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, day);
                String date = DATE_FMT.format(cal.getTime());

                // 上午排班（固定出诊）
                long morningScheduleId = insertSchedule(db, doctorId, date, "MORNING",
                        "08:00:00", "12:00:00");
                sourceDao.generateSlots(morningScheduleId, "08:00:00", "12:00:00");

                // 下午排班（固定出诊）
                long afternoonScheduleId = insertSchedule(db, doctorId, date, "AFTERNOON",
                        "13:00:00", "17:00:00");
                sourceDao.generateSlots(afternoonScheduleId, "13:00:00", "17:00:00");
            }
        }
    }

    private static long insertSchedule(SQLiteDatabase db, long doctorId, String date,
                                       String period, String startTime, String endTime) {
        ContentValues values = new ContentValues();
        values.put("doctor_id", doctorId);
        values.put("schedule_date", date);
        values.put("period", period);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("clinic_room", "B栋" + ((doctorId % 5) + 1) + "楼 诊室" + (doctorId % 10 + 1));
        values.put("schedule_status", 1);
        return db.insert("t_doctor_schedule", null, values);
    }

    // ==================== 5. 预约 ====================

    private static void insertAppointments(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // appointment_no, user_id, doctor_id, status
        String[][] data = {
                {"AP06250001", "1", "1",  "VISITED"},
                {"AP06250002", "1", "5",  "VISITED"},
                {"AP06260003", "1", "9",  "BOOKED"},
                {"AP06260004", "1", "15", "CANCELED"},
                {"AP06250005", "2", "3",  "VISITED"},
                {"AP06260006", "2", "13", "VISITED"},
                {"AP06270007", "3", "11", "BOOKED"},
                {"AP06270008", "3", "17", "VISITED"},
        };
        for (String[] row : data) {
            long userId = Long.parseLong(row[1]);
            long doctorId = Long.parseLong(row[2]);
            long sourceId = findAndOccupySource(dbHelper, doctorId);
            if (sourceId <= 0) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put("appointment_no", row[0]);
            values.put("user_id", userId);
            values.put("source_id", sourceId);
            values.put("appointment_status", row[3]);
            db.insert("t_appointment", null, values);
        }
    }

    /**
     * 查询指定医生的剩余号源（remain_num > 0），分配后立即扣减
     */
    private static long findAndOccupySource(DBHelper dbHelper, long doctorId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        RegisterSourceDao sourceDao = dbHelper.getRegisterSourceDao();

        Cursor c = db.rawQuery(
                "SELECT rs.id, rs.version FROM t_register_source rs "
                + "JOIN t_doctor_schedule ds ON rs.schedule_id = ds.id "
                + "WHERE ds.doctor_id = ? AND rs.remain_num > 0 "
                + "ORDER BY ds.schedule_date, rs.slot_start_time LIMIT 1",
                new String[]{String.valueOf(doctorId)});
        try {
            if (c.moveToFirst()) {
                long sourceId = c.getLong(0);
                int version = c.getInt(1);
                boolean success = sourceDao.decreaseRemainNum(sourceId, version);
                if (success) {
                    return sourceId;
                }
            }
        } finally {
            c.close();
        }
        return -1;
    }

    // ==================== 6. 医保卡 ====================

    private static void insertMedicalCards(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[][] data = {
                {"1", "张三", "320102********1234", "3201123456789012", "13800001111",
                        "8632.50",  "2026-01-01", "2027-01-01"},
                {"2", "李四", "320102********2345", "3201098765432109", "13800002222",
                        "12580.00", "2026-03-15", "2027-03-15"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("user_id", Long.parseLong(row[0]));
            values.put("holder_name", row[1]);
            values.put("id_card_no", row[2]);
            values.put("medical_card_no", row[3]);
            values.put("bind_phone", row[4]);
            values.put("balance", Double.parseDouble(row[5]));
            values.put("valid_start", row[6]);
            values.put("valid_end", row[7]);
            values.put("bind_status", "BOUND");
            db.insert("t_medical_card", null, values);
        }
    }

    // ==================== 7. 缴费单 ====================

    private static void insertPaymentOrders(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // order_no, appointment_id, user_id, medical_card_id, amount, pay_channel, order_status
        String[][] data = {
                {"ORD06250001", "1", "1", "1",           "15.00", "MEDICAL_CARD", "PAID"},
                {"ORD06250002", "2", "1", "1",           "15.00", "MEDICAL_CARD", "PAID"},
                {"ORD06260003", "3", "1", null,           "25.00", "MOCK_PAY",     "PAID"},
                {"ORD06260004", "4", "1", null,           "15.00", "MOCK_PAY",     "CANCELED"},
                {"ORD06250005", "5", "2", "2",           "15.00", "MEDICAL_CARD", "PAID"},
                {"ORD06260006", "6", "2", "2",           "25.00", "MEDICAL_CARD", "PAID"},
                {"ORD06270007", "7", "3", null,           "15.00", "MOCK_PAY",     "UNPAID"},
                {"ORD06270008", "8", "3", null,           "15.00", "MOCK_PAY",     "PAID"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("order_no", row[0]);
            values.put("appointment_id", Long.parseLong(row[1]));
            values.put("user_id", Long.parseLong(row[2]));
            if (row[3] != null) {
                values.put("medical_card_id", Long.parseLong(row[3]));
            }
            values.put("amount", Double.parseDouble(row[4]));
            values.put("pay_channel", row[5]);
            values.put("order_status", row[6]);
            if ("PAID".equals(row[6])) {
                values.put("pay_time", getCurrentDateTime());
            }
            db.insert("t_payment_order", null, values);
        }
    }

    // ==================== 8. 医保消费记录 ====================

    private static void insertMedicalCardRecords(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // card_id, user_id, related_order_id, record_type, amount, balance_after, description
        String[][] data = {
                {"1", "1", "1",   "OUTPATIENT", "156.00", "8476.50", "门诊统筹结算-全科门诊"},
                {"1", "1", null,   "PHARMACY",   "83.20",  "8393.30", "药房购药-维C银翘片"},
                {"1", "1", null,   "OTHER",      "120.00", "8273.30", "检查费用-心电图"},
                {"1", "1", "2",   "OUTPATIENT", "220.00", "8053.30", "门诊统筹结算-消化内科"},
                {"2", "2", "5",   "OUTPATIENT", "180.00", "12400.00","门诊统筹结算-呼吸内科"},
                {"2", "2", null,   "PHARMACY",   "65.50",  "12334.50","药房购药-头孢克肟"},
                {"2", "2", null,   "OTHER",      "200.00", "12134.50","检查费用-胸片"},
                {"2", "2", "6",   "OUTPATIENT", "95.00",  "12039.50","门诊统筹结算-妇产科"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("card_id", Long.parseLong(row[0]));
            values.put("user_id", Long.parseLong(row[1]));
            if (row[2] != null) {
                values.put("related_order_id", Long.parseLong(row[2]));
            }
            values.put("record_type", row[3]);
            values.put("amount", Double.parseDouble(row[4]));
            values.put("balance_after", Double.parseDouble(row[5]));
            values.put("description", row[6]);
            db.insert("t_medical_card_record", null, values);
        }
    }

    // ==================== 9. 就诊历史 ====================

    private static void insertVisitHistories(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // user_id, appointment_id, dept_id, doctor_id, visit_time, chief_complaint, diagnosis, treatment_advice
        String[][] data = {
                {"1", "1", "1", "1",  "2026-06-25 09:00",
                        "头晕、胸闷一周", "原发性高血压Ⅰ级", "低盐低脂饮食，规律服药，每日监测血压"},
                {"1", "2", "3", "5",  "2026-06-25 15:00",
                        "胃部不适、反酸", "慢性浅表性胃炎", "规律饮食，忌辛辣刺激，口服奥美拉唑"},
                {"2", "5", "2", "3",  "2026-06-25 10:00",
                        "咳嗽、发热三天", "急性支气管炎", "多饮水，注意休息，口服头孢克肟5天"},
                {"2", "6", "7", "13", "2026-06-26 09:30",
                        "停经8周", "早孕", "定期产检，口服叶酸，避免剧烈运动"},
                {"3", "8", "9", "17", "2026-06-27 14:00",
                        "全身皮疹、瘙痒", "过敏性荨麻疹", "避免过敏原，口服氯雷他定片"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("user_id", Long.parseLong(row[0]));
            values.put("appointment_id", Long.parseLong(row[1]));
            values.put("department_id", Long.parseLong(row[2]));
            values.put("doctor_id", Long.parseLong(row[3]));
            values.put("visit_time", row[4]);
            values.put("chief_complaint", row[5]);
            values.put("diagnosis", row[6]);
            values.put("treatment_advice", row[7]);
            db.insert("t_visit_history", null, values);
        }
    }

    // ==================== 10. 消息 ====================

    private static void insertMessages(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // user_id, title, content, business_type, is_read
        String[][] data = {
                {"1", "预约成功", "您已成功预约2026-06-25 心血管内科 张建国 主任医师",    "APPOINTMENT", "1"},
                {"1", "缴费成功", "预约AP06250001缴费成功，金额15.00元",                   "PAYMENT",     "1"},
                {"1", "系统公告", "端午节期间（6月22日-24日）门诊正常开放",                  "SYSTEM",      "0"},
                {"1", "预约提醒", "您明天下午有心血管内科的预约，请按时就诊",                 "APPOINTMENT", "0"},
                {"2", "预约成功", "您已成功预约2026-06-25 呼吸内科 王志强 主任医师",        "APPOINTMENT", "1"},
                {"2", "预约成功", "您已成功预约2026-06-26 妇产科 陈丽华 主任医师",          "APPOINTMENT", "1"},
                {"2", "系统公告", "医院新开设夜间门诊服务，时间为17:30-20:30",              "SYSTEM",      "0"},
                {"3", "预约成功", "您已成功预约2026-06-27 皮肤科 林文峰 主任医师",          "APPOINTMENT", "1"},
                {"3", "系统公告", "请关注'Serene Health'公众号获取更多健康资讯",            "SYSTEM",      "0"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("user_id", Long.parseLong(row[0]));
            values.put("title", row[1]);
            values.put("content", row[2]);
            values.put("business_type", row[3]);
            values.put("is_read", Integer.parseInt(row[4]));
            db.insert("t_message", null, values);
        }
    }

    // ==================== 11. 轮播图 ====================

    private static void insertBanners(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // title, image_url, jump_type, jump_value, sort_no, status
        String[][] data = {
                {"健康讲堂·糖尿病饮食",    "banner_health_talk",      "URL",  "https://www.health-example.com/diabetes", "1", "1"},
                {"夏季体检套餐优惠",       "banner_summer_checkup",   "NONE", null,                                         "2", "1"},
                {"端午节门诊安排通知",     "banner_dragon_boat",      "PAGE", "notice_detail",                              "3", "1"},
                {"疼痛科正式开诊",         "banner_new_department",   "NONE", null,                                         "4", "1"},
        };
        for (String[] row : data) {
            int sortNo = Integer.parseInt(row[4]);
            int status = Integer.parseInt(row[5]);
            if (bannerExists(db, row[0], row[1], row[2], row[3], sortNo, status)) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put("title", row[0]);
            values.put("image_url", row[1]);
            values.put("jump_type", row[2]);
            if (row[3] != null) {
                values.put("jump_value", row[3]);
            }
            values.put("sort_no", sortNo);
            values.put("status", status);
            db.insert("t_banner", null, values);
        }
    }

    // ==================== 12. 评价 ====================

    private static boolean bannerExists(SQLiteDatabase db, String title, String imageUrl,
                                        String jumpType, String jumpValue, int sortNo, int status) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT 1 FROM t_banner WHERE title = ? AND image_url = ? "
                            + "AND jump_type = ? AND COALESCE(jump_value, '') = ? "
                            + "AND sort_no = ? AND status = ? LIMIT 1",
                    new String[]{
                            title,
                            imageUrl,
                            jumpType,
                            jumpValue == null ? "" : jumpValue,
                            String.valueOf(sortNo),
                            String.valueOf(status)
                    });
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void insertFeedbacks(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // user_id, appointment_id, doctor_id, doctor_score, service_score, visit_score, content
        String[][] data = {
                {"1", "1", "1",  "5", "4", "5", "张主任态度很好，讲解详细，非常专业"},
                {"1", "2", "5",  "4", "3", "4", "就诊流程顺畅，就是等候时间略长"},
                {"2", "5", "3",  "5", "5", "5", "王医生非常耐心，开了药很快就好了"},
                {"2", "6", "13", "5", "5", "5", "陈主任产检很细致，让人安心"},
                {"3", "8", "17", "4", "4", "4", "诊断准确，用药后症状明显改善"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("user_id", Long.parseLong(row[0]));
            values.put("appointment_id", Long.parseLong(row[1]));
            values.put("doctor_id", Long.parseLong(row[2]));
            values.put("doctor_score", Integer.parseInt(row[3]));
            values.put("service_score", Integer.parseInt(row[4]));
            values.put("visit_score", Integer.parseInt(row[5]));
            values.put("content", row[6]);
            db.insert("t_feedback", null, values);
        }
    }

    // ==================== 13. 后台账号 ====================

    private static void insertAdminUsers(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // username, password, real_name, role_type, doctor_id, phone, status
        String[][] data = {
                {"admin", "123456", "管理员", "ADMIN", null, "13900000000", "1"},
                {"zhangjianguo", "123456", "张建国", "DOCTOR", "1", "13800000001", "1"},
                {"wangzhiqiang", "123456", "王志强", "DOCTOR", "3", "13800000002", "1"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("username", row[0]);
            values.put("password", row[1]);
            values.put("real_name", row[2]);
            values.put("role_type", row[3]);
            if (row[4] != null) {
                values.put("doctor_id", Long.parseLong(row[4]));
            }
            values.put("phone", row[5]);
            values.put("status", Integer.parseInt(row[6]));
            db.insert("t_admin_user", null, values);
        }
    }

    // ==================== 15. 帮助/隐私/流程内容 ====================

    private static void insertHelpContent(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // title, content_type, content, video_url, sort_no, status
        String[][] data = {
                {"挂号流程",       "REGISTER_FLOW", "1.选择科室→2.选择医生→3.选择日期时段→4.确认预约", null,                  "1", "1"},
                {"常见问题",       "HELP",          "Q:如何取消预约？A:进入预约记录-取消预约",           null,                  "2", "1"},
                {"隐私政策",       "PRIVACY",       "Serene Health重视您的隐私保护，所有医疗数据仅存储于本机...", null,          "3", "1"},
        };
        for (String[] row : data) {
            ContentValues values = new ContentValues();
            values.put("title", row[0]);
            values.put("content_type", row[1]);
            values.put("content", row[2]);
            if (row[3] != null) {
                values.put("video_url", row[3]);
            }
            values.put("sort_no", Integer.parseInt(row[4]));
            values.put("status", Integer.parseInt(row[5]));
            db.insert("t_help_content", null, values);
        }
    }

    // ==================== 14. 导诊规则 ====================

    private static void insertSymptomRules(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // symptom_keyword, department_id, recommend_reason
        String[][] data = {
                {"头痛",     "4",  "头痛多与神经系统相关，建议神经内科就诊"},
                {"头晕",     "4",  "头晕可能与脑血管或前庭功能有关"},
                {"胸闷",     "1",  "胸闷需排查心脏问题，建议心血管内科"},
                {"心悸",     "1",  "心悸高度关联心脏疾病"},
                {"胸痛",     "1",  "胸痛优先排查冠心病、心梗"},
                {"咳嗽",     "2",  "持续咳嗽建议呼吸内科检查"},
                {"咳痰",     "2",  "咳痰多与呼吸道感染相关"},
                {"气喘",     "2",  "气喘需排查哮喘或慢阻肺"},
                {"胃痛",     "3",  "胃痛多由消化系统疾病引起"},
                {"腹痛",     "3",  "腹痛建议消化内科排查"},
                {"腹泻",     "3",  "腹泻需消化内科诊治"},
                {"恶心",     "3",  "恶心可能与消化系统相关"},
                {"关节痛",   "5",  "关节疼痛建议骨科就诊"},
                {"腰疼",     "5",  "腰痛需排查骨科或脊柱问题"},
                {"骨折",     "5",  "骨折需骨科急诊处理"},
                {"发烧",     "8",  "儿童发烧建议儿科就诊"},
                {"皮疹",     "9",  "皮疹需皮肤科诊断"},
                {"皮肤痒",   "9",  "皮肤瘙痒多为皮肤科问题"},
                {"视力下降", "10", "视力下降需眼科检查"},
                {"眼睛红",   "10", "眼部红肿建议眼科"},
                {"牙疼",     "11", "牙痛需口腔科治疗"},
                {"牙龈出血", "11", "牙龈问题建议口腔科"},
                {"多饮多尿", "12", "多饮多尿是糖尿病典型症状，建议内分泌科"},
                {"月经不调", "7",  "月经问题建议妇产科"},
        };
        for (int i = 0; i < data.length; i++) {
            long departmentId = Long.parseLong(data[i][1]);
            if (symptomRuleExists(db, data[i][0], departmentId)) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put("symptom_keyword", data[i][0]);
            values.put("department_id", departmentId);
            values.put("recommend_reason", data[i][2]);
            values.put("sort_no", i + 1);
            db.insert("t_symptom_department_rule", null, values);
        }
    }

    private static boolean symptomRuleExists(SQLiteDatabase db, String symptomKeyword, long departmentId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT 1 FROM t_symptom_department_rule WHERE symptom_keyword = ? AND department_id = ? LIMIT 1",
                    new String[]{symptomKeyword, String.valueOf(departmentId)});
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String getCurrentDateTime() {
        return DATE_TIME_FMT.format(new Date());
    }
}
