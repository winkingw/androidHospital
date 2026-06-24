package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public UserDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 注册新用户，返回用户ID；手机号已存在返回-1
     */
    public long register(String phone, String password, String realName) {
        if (isPhoneExist(phone)) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put("phone", phone);
        values.put("password", password);
        values.put("real_name", realName);
        return db.insert("t_user", null, values);
    }

    /**
     * 登录验证，成功返回User对象，失败返回null
     */
    public User login(String phone, String password) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_user WHERE phone = ? AND password = ?",
                    new String[]{phone, password});
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查询用户
     */
    public User queryUserById(long userId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_user WHERE id = ?",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 更新用户信息（姓名、性别、生日等），返回影响行数
     */
    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put("real_name", user.getRealName());
        values.put("gender", user.getGender());
        values.put("birth_date", user.getBirthDate());
        values.put("id_card_no", user.getIdCardNo());
        values.put("health_score", user.getHealthScore());
        values.put("member_level", user.getMemberLevel());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_user", values, "id = ?",
                new String[]{String.valueOf(user.getId())});
    }

    /**
     * 修改密码
     */
    public int updatePassword(long userId, String oldPwd, String newPwd) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id FROM t_user WHERE id = ? AND password = ?",
                    new String[]{String.valueOf(userId), oldPwd});
            if (!cursor.moveToFirst()) {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put("password", newPwd);
        values.put("update_time", getCurrentDateTime());
        return db.update("t_user", values, "id = ?",
                new String[]{String.valueOf(userId)});
    }

    /**
     * 检查手机号是否已注册
     */
    public boolean isPhoneExist(String phone) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM t_user WHERE phone = ?",
                    new String[]{phone});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        if (!cursor.isNull(cursor.getColumnIndex("password"))) {
            user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
        }
        user.setRealName(cursor.getString(cursor.getColumnIndexOrThrow("real_name")));
        if (!cursor.isNull(cursor.getColumnIndex("gender"))) {
            user.setGender(cursor.getInt(cursor.getColumnIndex("gender")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("birth_date"))) {
            user.setBirthDate(cursor.getString(cursor.getColumnIndex("birth_date")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("id_card_no"))) {
            user.setIdCardNo(cursor.getString(cursor.getColumnIndex("id_card_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("health_score"))) {
            user.setHealthScore(cursor.getInt(cursor.getColumnIndex("health_score")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("member_level"))) {
            user.setMemberLevel(cursor.getString(cursor.getColumnIndex("member_level")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            user.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            user.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return user;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
