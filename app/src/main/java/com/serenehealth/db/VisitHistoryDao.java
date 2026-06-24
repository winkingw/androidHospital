package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.VisitHistory;

import java.util.ArrayList;
import java.util.List;

public class VisitHistoryDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public VisitHistoryDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查用户全部就诊历史，按就诊时间倒序
     */
    public List<VisitHistory> queryByUserId(long userId) {
        List<VisitHistory> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_visit_history WHERE user_id = ? ORDER BY visit_time DESC",
                    new String[]{String.valueOf(userId)});
            while (cursor.moveToNext()) {
                list.add(cursorToVisitHistory(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查某次预约对应的就诊历史
     */
    public VisitHistory queryByAppointmentId(long appointmentId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_visit_history WHERE appointment_id = ?",
                    new String[]{String.valueOf(appointmentId)});
            if (cursor.moveToFirst()) {
                return cursorToVisitHistory(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增就诊历史（预约完成后生成）
     */
    public long insert(VisitHistory history) {
        return insertInternal(history);
    }

    /**
     * 手动录入就诊历史（后台）
     */
    public long insertManual(VisitHistory history) {
        return insertInternal(history);
    }

    private long insertInternal(VisitHistory history) {
        ContentValues values = new ContentValues();
        values.put("user_id", history.getUserId());
        if (history.getAppointmentId() > 0) {
            values.put("appointment_id", history.getAppointmentId());
        }
        if (history.getDepartmentId() > 0) {
            values.put("department_id", history.getDepartmentId());
        }
        if (history.getDoctorId() > 0) {
            values.put("doctor_id", history.getDoctorId());
        }
        values.put("visit_time", history.getVisitTime());
        if (history.getChiefComplaint() != null) {
            values.put("chief_complaint", history.getChiefComplaint());
        }
        if (history.getDiagnosis() != null) {
            values.put("diagnosis", history.getDiagnosis());
        }
        if (history.getTreatmentAdvice() != null) {
            values.put("treatment_advice", history.getTreatmentAdvice());
        }
        if (history.getRemark() != null) {
            values.put("remark", history.getRemark());
        }
        return db.insert("t_visit_history", null, values);
    }

    private VisitHistory cursorToVisitHistory(Cursor cursor) {
        VisitHistory history = new VisitHistory();
        history.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        history.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        if (!cursor.isNull(cursor.getColumnIndex("appointment_id"))) {
            history.setAppointmentId(cursor.getLong(cursor.getColumnIndex("appointment_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("department_id"))) {
            history.setDepartmentId(cursor.getLong(cursor.getColumnIndex("department_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("doctor_id"))) {
            history.setDoctorId(cursor.getLong(cursor.getColumnIndex("doctor_id")));
        }
        history.setVisitTime(cursor.getString(cursor.getColumnIndexOrThrow("visit_time")));
        if (!cursor.isNull(cursor.getColumnIndex("chief_complaint"))) {
            history.setChiefComplaint(cursor.getString(cursor.getColumnIndex("chief_complaint")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("diagnosis"))) {
            history.setDiagnosis(cursor.getString(cursor.getColumnIndex("diagnosis")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("treatment_advice"))) {
            history.setTreatmentAdvice(cursor.getString(cursor.getColumnIndex("treatment_advice")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("remark"))) {
            history.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            history.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            history.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return history;
    }
}
