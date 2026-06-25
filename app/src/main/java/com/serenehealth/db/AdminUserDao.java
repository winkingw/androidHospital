package com.serenehealth.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.AdminUser;

public class AdminUserDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public AdminUserDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 登录验证，返回AdminUser对象，失败返回null
     */
    public AdminUser login(String username, String password) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_admin_user WHERE username = ? AND password = ?",
                    new String[]{username, password});
            if (cursor.moveToFirst()) {
                return cursorToAdminUser(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查询
     */
    public AdminUser queryById(long adminId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_admin_user WHERE id = ?",
                    new String[]{String.valueOf(adminId)});
            if (cursor.moveToFirst()) {
                return cursorToAdminUser(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据医生ID查对应的医生端账号
     */
    public AdminUser queryByDoctorId(long doctorId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_admin_user WHERE doctor_id = ?",
                    new String[]{String.valueOf(doctorId)});
            if (cursor.moveToFirst()) {
                return cursorToAdminUser(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private AdminUser cursorToAdminUser(Cursor cursor) {
        AdminUser user = new AdminUser();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setRealName(cursor.getString(cursor.getColumnIndexOrThrow("real_name")));
        user.setRoleType(cursor.getString(cursor.getColumnIndexOrThrow("role_type")));
        int doctorIdIndex = cursor.getColumnIndex("doctor_id");
        if (hasValue(cursor, doctorIdIndex)) {
            user.setDoctorId(cursor.getLong(doctorIdIndex));
        }
        int phoneIndex = cursor.getColumnIndex("phone");
        if (hasValue(cursor, phoneIndex)) {
            user.setPhone(cursor.getString(phoneIndex));
        }
        int statusIndex = cursor.getColumnIndex("status");
        if (hasValue(cursor, statusIndex)) {
            user.setStatus(cursor.getInt(statusIndex));
        }
        int lastLoginTimeIndex = cursor.getColumnIndex("last_login_time");
        if (hasValue(cursor, lastLoginTimeIndex)) {
            user.setLastLoginTime(cursor.getString(lastLoginTimeIndex));
        }
        int createTimeIndex = cursor.getColumnIndex("create_time");
        if (hasValue(cursor, createTimeIndex)) {
            user.setCreateTime(cursor.getString(createTimeIndex));
        }
        int updateTimeIndex = cursor.getColumnIndex("update_time");
        if (hasValue(cursor, updateTimeIndex)) {
            user.setUpdateTime(cursor.getString(updateTimeIndex));
        }
        return user;
    }

    private boolean hasValue(Cursor cursor, int columnIndex) {
        return columnIndex >= 0 && !cursor.isNull(columnIndex);
    }
}
