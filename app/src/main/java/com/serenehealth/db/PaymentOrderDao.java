package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.PaymentOrder;

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
        return order;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
