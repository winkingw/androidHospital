package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DoctorDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询某科室下所有在职医生
     */
    public List<Doctor> queryDoctorsByDepartment(long deptId) {
        List<Doctor> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor WHERE department_id = ? ORDER BY sort_no ASC",
                    new String[]{String.valueOf(deptId)});
            while (cursor.moveToNext()) {
                list.add(cursorToDoctor(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查医生详情
     */
    public Doctor queryDoctorById(long doctorId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor WHERE id = ?",
                    new String[]{String.valueOf(doctorId)});
            if (cursor.moveToFirst()) {
                return cursorToDoctor(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 按姓名搜索医生
     */
    public List<Doctor> searchDoctors(String keyword) {
        List<Doctor> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_doctor WHERE doctor_name LIKE ? AND status = 1",
                    new String[]{"%" + keyword + "%"});
            while (cursor.moveToNext()) {
                list.add(cursorToDoctor(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增医生
     */
    public long insertDoctor(Doctor doctor) {
        ContentValues values = new ContentValues();
        values.put("department_id", doctor.getDepartmentId());
        values.put("doctor_name", doctor.getDoctorName());
        values.put("gender", doctor.getGender());
        if (doctor.getTitle() != null) {
            values.put("title", doctor.getTitle());
        }
        if (doctor.getIntroduction() != null) {
            values.put("introduction", doctor.getIntroduction());
        }
        values.put("status", doctor.getStatus());
        values.put("sort_no", doctor.getSortNo());
        return db.insert("t_doctor", null, values);
    }

    /**
     * 更新医生信息
     */
    public int updateDoctor(Doctor doctor) {
        ContentValues values = new ContentValues();
        values.put("department_id", doctor.getDepartmentId());
        values.put("doctor_name", doctor.getDoctorName());
        values.put("gender", doctor.getGender());
        if (doctor.getTitle() != null) {
            values.put("title", doctor.getTitle());
        }
        if (doctor.getIntroduction() != null) {
            values.put("introduction", doctor.getIntroduction());
        }
        values.put("status", doctor.getStatus());
        values.put("sort_no", doctor.getSortNo());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_doctor", values, "id = ?",
                new String[]{String.valueOf(doctor.getId())});
    }

    /**
     * 删除医生（停用：status=0）
     */
    public int disableDoctor(long doctorId) {
        ContentValues values = new ContentValues();
        values.put("status", 0);
        values.put("update_time", getCurrentDateTime());
        return db.update("t_doctor", values, "id = ?",
                new String[]{String.valueOf(doctorId)});
    }

    private Doctor cursorToDoctor(Cursor cursor) {
        Doctor doctor = new Doctor();
        doctor.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        doctor.setDepartmentId(cursor.getLong(cursor.getColumnIndexOrThrow("department_id")));
        doctor.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
        if (!cursor.isNull(cursor.getColumnIndex("gender"))) {
            doctor.setGender(cursor.getInt(cursor.getColumnIndex("gender")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("title"))) {
            doctor.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("introduction"))) {
            doctor.setIntroduction(cursor.getString(cursor.getColumnIndex("introduction")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("status"))) {
            doctor.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            doctor.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            doctor.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            doctor.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return doctor;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
