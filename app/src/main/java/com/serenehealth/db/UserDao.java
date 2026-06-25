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
        values.put("id_card_front_uri", user.getIdCardFrontUri());
        values.put("id_card_back_uri", user.getIdCardBackUri());
        values.put("real_name_verified", user.getRealNameVerified());
        values.put("health_score", user.getHealthScore());
        values.put("member_level", user.getMemberLevel());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_user", values, "id = ?",
                new String[]{String.valueOf(user.getId())});
    }

    public int updateRealNameVerification(long userId, String realName, String idCardNo) {
        return updateRealNameVerification(userId, realName, idCardNo, null, null);
    }

    public int updateRealNameVerification(long userId, String realName, String idCardNo,
                                          String frontUri, String backUri) {
        ContentValues values = new ContentValues();
        values.put("real_name", realName);
        values.put("id_card_no", idCardNo);
        values.put("id_card_front_uri", frontUri);
        values.put("id_card_back_uri", backUri);
        values.put("real_name_verified", 1);
        values.put("update_time", getCurrentDateTime());
        try {
            return db.update("t_user", values, "id = ?",
                    new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            return 0;
        }
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
        int passwordIndex = cursor.getColumnIndex("password");
        if (hasValue(cursor, passwordIndex)) {
            user.setPassword(cursor.getString(passwordIndex));
        }
        user.setRealName(cursor.getString(cursor.getColumnIndexOrThrow("real_name")));
        int genderIndex = cursor.getColumnIndex("gender");
        if (hasValue(cursor, genderIndex)) {
            user.setGender(cursor.getInt(genderIndex));
        }
        int birthDateIndex = cursor.getColumnIndex("birth_date");
        if (hasValue(cursor, birthDateIndex)) {
            user.setBirthDate(cursor.getString(birthDateIndex));
        }
        int idCardNoIndex = cursor.getColumnIndex("id_card_no");
        if (hasValue(cursor, idCardNoIndex)) {
            user.setIdCardNo(cursor.getString(idCardNoIndex));
        }
        int idCardFrontUriIndex = cursor.getColumnIndex("id_card_front_uri");
        if (hasValue(cursor, idCardFrontUriIndex)) {
            user.setIdCardFrontUri(cursor.getString(idCardFrontUriIndex));
        }
        int idCardBackUriIndex = cursor.getColumnIndex("id_card_back_uri");
        if (hasValue(cursor, idCardBackUriIndex)) {
            user.setIdCardBackUri(cursor.getString(idCardBackUriIndex));
        }
        int verifiedIndex = cursor.getColumnIndex("real_name_verified");
        if (hasValue(cursor, verifiedIndex)) {
            user.setRealNameVerified(cursor.getInt(verifiedIndex));
        }
        int healthScoreIndex = cursor.getColumnIndex("health_score");
        if (hasValue(cursor, healthScoreIndex)) {
            user.setHealthScore(cursor.getInt(healthScoreIndex));
        }
        int memberLevelIndex = cursor.getColumnIndex("member_level");
        if (hasValue(cursor, memberLevelIndex)) {
            user.setMemberLevel(cursor.getString(memberLevelIndex));
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

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
