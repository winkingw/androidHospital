package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.DoctorSchedule;

import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DoctorScheduleDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询某医生某日的排班（上/下午）
     */
    public List<DoctorSchedule> querySchedulesByDoctor(long doctorId, String date) {
        List<DoctorSchedule> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor_schedule WHERE doctor_id = ? AND schedule_date = ?",
                    new String[]{String.valueOf(doctorId), date});
            while (cursor.moveToNext()) {
                list.add(cursorToDoctorSchedule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询某医生某日可出诊排班，用户端预约只展示正常排班。
     */
    public List<DoctorSchedule> queryActiveSchedulesByDoctor(long doctorId, String date) {
        List<DoctorSchedule> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM t_doctor_schedule "
                            + "WHERE doctor_id = ? AND schedule_date = ? AND schedule_status = 1",
                    new String[]{String.valueOf(doctorId), date});
            while (cursor.moveToNext()) {
                list.add(cursorToDoctorSchedule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询医生最近一次非空诊室，用于新生成排班时保持诊室一致。
     */
    public String queryLatestClinicRoom(long doctorId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT clinic_room FROM t_doctor_schedule "
                            + "WHERE doctor_id = ? AND clinic_room IS NOT NULL AND TRIM(clinic_room) != '' "
                            + "ORDER BY schedule_date DESC, update_time DESC, id DESC LIMIT 1",
                    new String[]{String.valueOf(doctorId)});
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 批量查询多位医生在日期范围内的排班
     */
    public List<DoctorSchedule> querySchedulesByDoctorIdsAndDateRange(List<Long> doctorIds, String startDate, String endDate) {
        List<DoctorSchedule> list = new ArrayList<>();
        if (doctorIds == null || doctorIds.isEmpty()) {
            return list;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < doctorIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[doctorIds.size() + 2];
            for (int i = 0; i < doctorIds.size(); i++) {
                selectionArgs[i] = String.valueOf(doctorIds.get(i));
            }
            selectionArgs[doctorIds.size()] = startDate;
            selectionArgs[doctorIds.size() + 1] = endDate;
            cursor = db.rawQuery(
                    "SELECT * FROM t_doctor_schedule WHERE doctor_id IN (" + placeholders + ") AND schedule_date BETWEEN ? AND ?",
                    selectionArgs);
            while (cursor.moveToNext()) {
                list.add(cursorToDoctorSchedule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询某日期某科室所有医生的排班
     */
    public List<DoctorSchedule> querySchedulesByDeptAndDate(long deptId, String date) {
        List<DoctorSchedule> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT ds.* FROM t_doctor_schedule ds "
                            + "JOIN t_doctor d ON ds.doctor_id = d.id "
                            + "WHERE d.department_id = ? AND ds.schedule_date = ?",
                    new String[]{String.valueOf(deptId), date});
            while (cursor.moveToNext()) {
                list.add(cursorToDoctorSchedule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查排班
     */
    public DoctorSchedule queryScheduleById(long scheduleId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor_schedule WHERE id = ?",
                    new String[]{String.valueOf(scheduleId)});
            if (cursor.moveToFirst()) {
                return cursorToDoctorSchedule(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增排班
     */
    public long insertSchedule(DoctorSchedule schedule) {
        ContentValues values = new ContentValues();
        values.put("doctor_id", schedule.getDoctorId());
        values.put("schedule_date", schedule.getScheduleDate());
        values.put("period", schedule.getPeriod());
        if (schedule.getClinicRoom() != null) {
            values.put("clinic_room", schedule.getClinicRoom());
        }
        values.put("start_time", schedule.getStartTime());
        values.put("end_time", schedule.getEndTime());
        values.put("schedule_status", schedule.getScheduleStatus());
        return db.insert("t_doctor_schedule", null, values);
    }

    /**
     * 编辑排班
     */
    public int updateSchedule(DoctorSchedule schedule) {
        ContentValues values = new ContentValues();
        values.put("doctor_id", schedule.getDoctorId());
        values.put("schedule_date", schedule.getScheduleDate());
        values.put("period", schedule.getPeriod());
        if (schedule.getClinicRoom() != null) {
            values.put("clinic_room", schedule.getClinicRoom());
        }
        values.put("start_time", schedule.getStartTime());
        values.put("end_time", schedule.getEndTime());
        values.put("schedule_status", schedule.getScheduleStatus());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_doctor_schedule", values, "id = ?",
                new String[]{String.valueOf(schedule.getId())});
    }

    /**
     * 停诊
     */
    public int cancelSchedule(long scheduleId) {
        ContentValues values = new ContentValues();
        values.put("schedule_status", 0);
        values.put("update_time", getCurrentDateTime());
        return db.update("t_doctor_schedule", values, "id = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    /**
     * 恢复出诊
     */
    public int restoreSchedule(long scheduleId) {
        ContentValues values = new ContentValues();
        values.put("schedule_status", 1);
        values.put("update_time", getCurrentDateTime());
        return db.update("t_doctor_schedule", values, "id = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    /**
     * 物理删除排班行（用于重新生成前清理旧数据，避免 UNIQUE 约束冲突）
     */
    public int deleteSchedule(long scheduleId) {
        return db.delete("t_doctor_schedule", "id = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    private DoctorSchedule cursorToDoctorSchedule(Cursor cursor) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        schedule.setDoctorId(cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")));
        schedule.setScheduleDate(cursor.getString(cursor.getColumnIndexOrThrow("schedule_date")));
        schedule.setPeriod(cursor.getString(cursor.getColumnIndexOrThrow("period")));
        if (!cursor.isNull(cursor.getColumnIndex("clinic_room"))) {
            schedule.setClinicRoom(cursor.getString(cursor.getColumnIndex("clinic_room")));
        }
        schedule.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
        schedule.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("end_time")));
        if (!cursor.isNull(cursor.getColumnIndex("schedule_status"))) {
            schedule.setScheduleStatus(cursor.getInt(cursor.getColumnIndex("schedule_status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            schedule.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            schedule.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return schedule;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
