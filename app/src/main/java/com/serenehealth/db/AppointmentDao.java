package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Appointment;
import com.serenehealth.bean.Message;
import com.serenehealth.bean.PaymentOrder;
import com.serenehealth.bean.RegisterSource;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public AppointmentDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 创建预约（事务包裹）：
     * 1. 扣减号源 remain_num（乐观锁）
     * 2. 插入预约记录
     * 3. 生成缴费订单
     * 4. 生成消息通知
     * 返回预约ID；号源不足返回-1
     */
    public long createAppointment(Appointment appointment) {
        long appointmentId = -1;
        db.beginTransaction();
        try {
            RegisterSourceDao sourceDao = dbHelper.getRegisterSourceDao();
            RegisterSource source = sourceDao.querySourceById(appointment.getSourceId());
            if (source != null && source.getRemainNum() > 0) {
                boolean decreased = sourceDao.decreaseRemainNum(source.getId(), source.getVersion());
                if (decreased) {
                    ContentValues appointValues = new ContentValues();
                    appointValues.put("appointment_no", appointment.getAppointmentNo());
                    appointValues.put("user_id", appointment.getUserId());
                    appointValues.put("source_id", appointment.getSourceId());
                    appointValues.put("appointment_status", "BOOKED");
                    appointmentId = db.insert("t_appointment", null, appointValues);
                    if (appointmentId != -1) {
                        PaymentOrder order = new PaymentOrder();
                        order.setOrderNo(generateOrderNo());
                        order.setAppointmentId(appointmentId);
                        order.setUserId(appointment.getUserId());
                        order.setAmount(source.getRegisterFee());
                        order.setPayChannel("MOCK_PAY");
                        order.setOrderStatus("UNPAID");
                        dbHelper.getPaymentOrderDao().createOrder(order);

                        Message message = new Message();
                        message.setUserId(appointment.getUserId());
                        message.setTitle("预约成功通知");
                        message.setContent("您已成功预约，预约编号：" + appointment.getAppointmentNo()
                                + "，请按时就诊。");
                        message.setBusinessType("APPOINTMENT");
                        message.setBusinessId(appointmentId);
                        message.setIsRead(0);
                        message.setSendTime(getCurrentDateTime());
                        dbHelper.getMessageDao().insert(message);

                        db.setTransactionSuccessful();
                    }
                }
            }
        } finally {
            db.endTransaction();
        }
        return appointmentId;
    }

    /**
     * 查询用户全部预约，按时间倒序
     */
    public List<Appointment> queryAppointmentsByUser(long userId) {
        List<Appointment> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_appointment WHERE user_id = ? ORDER BY create_time DESC",
                    new String[]{String.valueOf(userId)});
            while (cursor.moveToNext()) {
                list.add(cursorToAppointment(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 按状态筛选预约
     */
    public List<Appointment> queryAppointmentsByStatus(long userId, String status) {
        List<Appointment> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_appointment WHERE user_id = ? AND appointment_status = ? ORDER BY create_time DESC",
                    new String[]{String.valueOf(userId), status});
            while (cursor.moveToNext()) {
                list.add(cursorToAppointment(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查预约详情
     */
    public Appointment queryAppointmentDetail(long appointmentId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_appointment WHERE id = ?",
                    new String[]{String.valueOf(appointmentId)});
            if (cursor.moveToFirst()) {
                return cursorToAppointment(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 取消预约（事务包裹）：
     * 1. 状态→CANCELED
     * 2. 恢复号源
     * 3. 更新支付订单状态
     * 4. 生成消息通知
     */
    public boolean cancelAppointment(long appointmentId, String reason) {
        db.beginTransaction();
        try {
            // 1. 查询预约获取号源ID
            Appointment appointment = queryAppointmentDetail(appointmentId);
            if (appointment == null) {
                return false;
            }

            // 2. 状态→CANCELED
            ContentValues appointValues = new ContentValues();
            appointValues.put("appointment_status", "CANCELED");
            appointValues.put("cancel_reason", reason);
            appointValues.put("update_time", getCurrentDateTime());
            int updated = db.update("t_appointment", appointValues, "id = ?",
                    new String[]{String.valueOf(appointmentId)});
            if (updated <= 0) {
                return false;
            }

            // 3. 恢复号源
            dbHelper.getRegisterSourceDao().increaseRemainNum(appointment.getSourceId());

            // 4. 更新支付订单状态
            PaymentOrder order = dbHelper.getPaymentOrderDao().queryOrderByAppointment(appointmentId);
            if (order != null) {
                dbHelper.getPaymentOrderDao().cancelOrder(order.getId());
            }

            // 5. 生成消息通知
            Message message = new Message();
            message.setUserId(appointment.getUserId());
            message.setTitle("预约取消通知");
            message.setContent("您的预约（编号：" + appointment.getAppointmentNo() + "）已取消"
                    + (reason != null && !reason.isEmpty() ? "，原因：" + reason : "") + "。");
            message.setBusinessType("APPOINTMENT");
            message.setBusinessId(appointmentId);
            message.setIsRead(0);
            message.setSendTime(getCurrentDateTime());
            dbHelper.getMessageDao().insert(message);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 将预约状态改为VISITED（就诊完成）
     */
    public int completeAppointment(long appointmentId) {
        ContentValues values = new ContentValues();
        values.put("appointment_status", "VISITED");
        values.put("update_time", getCurrentDateTime());
        return db.update("t_appointment", values, "id = ?",
                new String[]{String.valueOf(appointmentId)});
    }

    /**
     * 将预约状态改为EXPIRED（过期未就诊）
     */
    public int expireAppointment(long appointmentId) {
        ContentValues values = new ContentValues();
        values.put("appointment_status", "EXPIRED");
        values.put("update_time", getCurrentDateTime());
        return db.update("t_appointment", values, "id = ?",
                new String[]{String.valueOf(appointmentId)});
    }

    /**
     * 医生端：查某医生名下的预约
     */
    public List<Appointment> queryAppointmentsByDoctor(long doctorId) {
        List<Appointment> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT a.* FROM t_appointment a "
                            + "JOIN t_register_source rs ON a.source_id = rs.id "
                            + "JOIN t_doctor_schedule ds ON rs.schedule_id = ds.id "
                            + "WHERE ds.doctor_id = ? ORDER BY a.create_time DESC",
                    new String[]{String.valueOf(doctorId)});
            while (cursor.moveToNext()) {
                list.add(cursorToAppointment(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment();
        appointment.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        appointment.setAppointmentNo(cursor.getString(cursor.getColumnIndexOrThrow("appointment_no")));
        appointment.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        appointment.setSourceId(cursor.getLong(cursor.getColumnIndexOrThrow("source_id")));
        if (!cursor.isNull(cursor.getColumnIndex("appointment_status"))) {
            appointment.setAppointmentStatus(cursor.getString(cursor.getColumnIndex("appointment_status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("cancel_reason"))) {
            appointment.setCancelReason(cursor.getString(cursor.getColumnIndex("cancel_reason")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            appointment.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            appointment.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return appointment;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
