package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.MedicalCard;
import com.serenehealth.bean.MedicalCardRecord;
import com.serenehealth.bean.PaymentOrder;

import java.util.ArrayList;
import java.util.List;

public class PaymentOrderDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public PaymentOrderDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 创建缴费订单，返回订单ID
     */
    public long createOrder(PaymentOrder order) {
        ContentValues values = new ContentValues();
        values.put("order_no", order.getOrderNo());
        values.put("appointment_id", order.getAppointmentId());
        values.put("user_id", order.getUserId());
        if (order.getMedicalCardId() > 0) {
            values.put("medical_card_id", order.getMedicalCardId());
        }
        values.put("amount", order.getAmount());
        if (order.getPayChannel() != null) {
            values.put("pay_channel", order.getPayChannel());
        }
        if (order.getOrderStatus() != null) {
            values.put("order_status", order.getOrderStatus());
        }
        return db.insert("t_payment_order", null, values);
    }

    /**
     * 根据预约ID查订单
     */
    public PaymentOrder queryOrderByAppointment(long appointmentId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_payment_order WHERE appointment_id = ?",
                    new String[]{String.valueOf(appointmentId)});
            if (cursor.moveToFirst()) {
                return cursorToPaymentOrder(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据用户查询全部缴费订单，按创建时间倒序
     */
    public List<PaymentOrder> queryOrdersByUser(long userId) {
        List<PaymentOrder> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT po.*, a.appointment_no, "
                            + "rs.slot_start_time, rs.slot_end_time, "
                            + "ds.schedule_date, ds.period, ds.clinic_room, "
                            + "d.doctor_name, dep.dept_name AS department_name "
                            + "FROM t_payment_order po "
                            + "LEFT JOIN t_appointment a ON po.appointment_id = a.id "
                            + "LEFT JOIN t_register_source rs ON a.source_id = rs.id "
                            + "LEFT JOIN t_doctor_schedule ds ON rs.schedule_id = ds.id "
                            + "LEFT JOIN t_doctor d ON ds.doctor_id = d.id "
                            + "LEFT JOIN t_department dep ON d.department_id = dep.id "
                            + "WHERE po.user_id = ? "
                            + "ORDER BY po.create_time DESC",
                    new String[]{String.valueOf(userId)});
            while (cursor.moveToNext()) {
                list.add(cursorToPaymentOrder(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据订单ID查询订单
     */
    public PaymentOrder queryOrderById(long orderId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_payment_order WHERE id = ?",
                    new String[]{String.valueOf(orderId)});
            if (cursor.moveToFirst()) {
                return cursorToPaymentOrder(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 模拟支付成功：状态→PAID，记录支付时间
     */
    public int payOrder(long orderId, String payChannel) {
        ContentValues values = new ContentValues();
        values.put("order_status", "PAID");
        values.put("pay_channel", payChannel);
        values.put("pay_time", getCurrentDateTime());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_payment_order", values, "id = ?",
                new String[]{String.valueOf(orderId)});
    }

    /**
     * 医保卡支付：订单、医保卡余额、消费记录在同一事务中同步更新。
     */
    public boolean payOrderWithMedicalCard(long orderId, MedicalCard card) {
        PaymentOrder order = queryOrderById(orderId);
        if (order == null || card == null || !"UNPAID".equals(order.getOrderStatus())) {
            return false;
        }
        if (card.getBalance() < order.getAmount()) {
            return false;
        }

        db.beginTransaction();
        try {
            double newBalance = card.getBalance() - order.getAmount();
            ContentValues orderValues = new ContentValues();
            orderValues.put("order_status", "PAID");
            orderValues.put("pay_channel", "MEDICAL_CARD");
            orderValues.put("medical_card_id", card.getId());
            orderValues.put("pay_time", getCurrentDateTime());
            orderValues.put("update_time", getCurrentDateTime());
            int orderRows = db.update("t_payment_order", orderValues, "id = ?",
                    new String[]{String.valueOf(orderId)});
            if (orderRows <= 0) {
                return false;
            }

            int cardRows = dbHelper.getMedicalCardDao().updateBalance(card.getUserId(), newBalance);
            if (cardRows <= 0) {
                return false;
            }

            MedicalCardRecord record = new MedicalCardRecord();
            record.setCardId(card.getId());
            record.setUserId(card.getUserId());
            record.setRelatedOrderId(orderId);
            record.setRecordType("REGISTER");
            record.setAmount(order.getAmount());
            record.setBalanceAfter(newBalance);
            record.setDescription("医保卡支付-" + order.getOrderNo());
            record.setRecordTime(getCurrentDateTime());
            long recordId = dbHelper.getMedicalCardRecordDao().insert(record);
            if (recordId == -1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 取消订单
     */
    public int cancelOrder(long orderId) {
        ContentValues values = new ContentValues();
        values.put("order_status", "CANCELED");
        values.put("update_time", getCurrentDateTime());
        return db.update("t_payment_order", values, "id = ?",
                new String[]{String.valueOf(orderId)});
    }

    private PaymentOrder cursorToPaymentOrder(Cursor cursor) {
        PaymentOrder order = new PaymentOrder();
        order.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        order.setOrderNo(cursor.getString(cursor.getColumnIndexOrThrow("order_no")));
        order.setAppointmentId(cursor.getLong(cursor.getColumnIndexOrThrow("appointment_id")));
        order.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        if (!cursor.isNull(cursor.getColumnIndex("medical_card_id"))) {
            order.setMedicalCardId(cursor.getLong(cursor.getColumnIndex("medical_card_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("amount"))) {
            order.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("pay_channel"))) {
            order.setPayChannel(cursor.getString(cursor.getColumnIndex("pay_channel")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("order_status"))) {
            order.setOrderStatus(cursor.getString(cursor.getColumnIndex("order_status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("pay_time"))) {
            order.setPayTime(cursor.getString(cursor.getColumnIndex("pay_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            order.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            order.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        int appointmentNoIndex = cursor.getColumnIndex("appointment_no");
        if (hasValue(cursor, appointmentNoIndex)) {
            order.setAppointmentNo(cursor.getString(appointmentNoIndex));
        }
        int departmentNameIndex = cursor.getColumnIndex("department_name");
        if (hasValue(cursor, departmentNameIndex)) {
            order.setDepartmentName(cursor.getString(departmentNameIndex));
        }
        int doctorNameIndex = cursor.getColumnIndex("doctor_name");
        if (hasValue(cursor, doctorNameIndex)) {
            order.setDoctorName(cursor.getString(doctorNameIndex));
        }
        int scheduleDateIndex = cursor.getColumnIndex("schedule_date");
        if (hasValue(cursor, scheduleDateIndex)) {
            order.setScheduleDate(cursor.getString(scheduleDateIndex));
        }
        int periodIndex = cursor.getColumnIndex("period");
        if (hasValue(cursor, periodIndex)) {
            order.setPeriod(cursor.getString(periodIndex));
        }
        int clinicRoomIndex = cursor.getColumnIndex("clinic_room");
        if (hasValue(cursor, clinicRoomIndex)) {
            order.setClinicRoom(cursor.getString(clinicRoomIndex));
        }
        int slotStartTimeIndex = cursor.getColumnIndex("slot_start_time");
        if (hasValue(cursor, slotStartTimeIndex)) {
            order.setSlotStartTime(cursor.getString(slotStartTimeIndex));
        }
        int slotEndTimeIndex = cursor.getColumnIndex("slot_end_time");
        if (hasValue(cursor, slotEndTimeIndex)) {
            order.setSlotEndTime(cursor.getString(slotEndTimeIndex));
        }
        return order;
    }

    private boolean hasValue(Cursor cursor, int columnIndex) {
        return columnIndex >= 0 && !cursor.isNull(columnIndex);
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
