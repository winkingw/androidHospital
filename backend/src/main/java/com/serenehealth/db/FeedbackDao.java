package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Feedback;

import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public FeedbackDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查医生收到的评价列表
     */
    public List<Feedback> queryByDoctorId(long doctorId) {
        List<Feedback> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_feedback WHERE doctor_id = ? ORDER BY create_time DESC",
                    new String[]{String.valueOf(doctorId)});
            while (cursor.moveToNext()) {
                list.add(cursorToFeedback(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查某预约对应的评价
     */
    public Feedback queryByAppointmentId(long appointmentId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_feedback WHERE appointment_id = ?",
                    new String[]{String.valueOf(appointmentId)});
            if (cursor.moveToFirst()) {
                return cursorToFeedback(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 提交评价（三维评分）
     */
    public long insert(Feedback feedback) {
        ContentValues values = new ContentValues();
        values.put("user_id", feedback.getUserId());
        if (feedback.getAppointmentId() > 0) {
            values.put("appointment_id", feedback.getAppointmentId());
        }
        if (feedback.getDoctorId() > 0) {
            values.put("doctor_id", feedback.getDoctorId());
        }
        values.put("doctor_score", feedback.getDoctorScore());
        values.put("service_score", feedback.getServiceScore());
        values.put("visit_score", feedback.getVisitScore());
        if (feedback.getContent() != null) {
            values.put("content", feedback.getContent());
        }
        return db.insert("t_feedback", null, values);
    }

    private Feedback cursorToFeedback(Cursor cursor) {
        Feedback feedback = new Feedback();
        feedback.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        feedback.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        if (!cursor.isNull(cursor.getColumnIndex("appointment_id"))) {
            feedback.setAppointmentId(cursor.getLong(cursor.getColumnIndex("appointment_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("doctor_id"))) {
            feedback.setDoctorId(cursor.getLong(cursor.getColumnIndex("doctor_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("doctor_score"))) {
            feedback.setDoctorScore(cursor.getInt(cursor.getColumnIndex("doctor_score")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("service_score"))) {
            feedback.setServiceScore(cursor.getInt(cursor.getColumnIndex("service_score")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("visit_score"))) {
            feedback.setVisitScore(cursor.getInt(cursor.getColumnIndex("visit_score")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("content"))) {
            feedback.setContent(cursor.getString(cursor.getColumnIndex("content")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            feedback.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        return feedback;
    }
}
