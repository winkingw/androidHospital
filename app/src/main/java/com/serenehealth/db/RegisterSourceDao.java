package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.serenehealth.bean.RegisterSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegisterSourceDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    private static final int SLOT_MINUTES = 20;

    public RegisterSourceDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询某排班下的所有20分钟号源段
     */
    public List<RegisterSource> querySourcesBySchedule(long scheduleId) {
        List<RegisterSource> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_register_source WHERE schedule_id = ? ORDER BY slot_start_time ASC",
                    new String[]{String.valueOf(scheduleId)});
            while (cursor.moveToNext()) {
                list.add(cursorToRegisterSource(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查号源
     */
    public RegisterSource querySourceById(long sourceId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_register_source WHERE id = ?",
                    new String[]{String.valueOf(sourceId)});
            if (cursor.moveToFirst()) {
                return cursorToRegisterSource(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 扣减号源（带乐观锁），成功返回true，已满返回false
     */
    public boolean decreaseRemainNum(long sourceId, int currentVersion) {
        String now = getCurrentDateTime();
        db.execSQL("UPDATE t_register_source SET remain_num = remain_num - 1, version = version + 1, update_time = ? "
                        + "WHERE id = ? AND version = ? AND remain_num > 0",
                new Object[]{now, sourceId, currentVersion});
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT changes()", null);
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

    /**
     * 恢复号源（取消预约时）
     */
    public void increaseRemainNum(long sourceId) {
        db.execSQL("UPDATE t_register_source SET remain_num = remain_num + 1, update_time = ? WHERE id = ?",
                new Object[]{getCurrentDateTime(), sourceId});
    }

    /**
     * 批量生成20分钟号源（排班创建时自动调用）
     */
    public void generateSlots(long scheduleId, String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            while (cal.getTime().before(end)) {
                String slotStart = sdf.format(cal.getTime());
                cal.add(Calendar.MINUTE, SLOT_MINUTES);
                Date slotEndDate = cal.getTime();
                if (slotEndDate.after(end)) {
                    break;
                }
                String slotEnd = sdf.format(slotEndDate);

                ContentValues values = new ContentValues();
                values.put("schedule_id", scheduleId);
                values.put("slot_start_time", slotStart);
                values.put("slot_end_time", slotEnd);
                values.put("total_num", 1);
                values.put("remain_num", 1);
                values.put("register_fee", 20.00);
                values.put("source_status", 1);
                values.put("version", 0);
                db.insert("t_register_source", null, values);
            }
        } catch (java.text.ParseException e) {
            Log.e("RegisterSourceDao", "生成号源时时间解析失败", e);
        }
    }

    /**
     * 编辑号源
     */
    public int updateSource(RegisterSource source) {
        ContentValues values = new ContentValues();
        values.put("schedule_id", source.getScheduleId());
        values.put("slot_start_time", source.getSlotStartTime());
        values.put("slot_end_time", source.getSlotEndTime());
        values.put("total_num", source.getTotalNum());
        values.put("remain_num", source.getRemainNum());
        values.put("register_fee", source.getRegisterFee());
        values.put("source_status", source.getSourceStatus());
        values.put("version", source.getVersion());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_register_source", values, "id = ?",
                new String[]{String.valueOf(source.getId())});
    }

    private RegisterSource cursorToRegisterSource(Cursor cursor) {
        RegisterSource source = new RegisterSource();
        source.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        source.setScheduleId(cursor.getLong(cursor.getColumnIndexOrThrow("schedule_id")));
        source.setSlotStartTime(cursor.getString(cursor.getColumnIndexOrThrow("slot_start_time")));
        source.setSlotEndTime(cursor.getString(cursor.getColumnIndexOrThrow("slot_end_time")));
        if (!cursor.isNull(cursor.getColumnIndex("total_num"))) {
            source.setTotalNum(cursor.getInt(cursor.getColumnIndex("total_num")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("remain_num"))) {
            source.setRemainNum(cursor.getInt(cursor.getColumnIndex("remain_num")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("register_fee"))) {
            source.setRegisterFee(cursor.getDouble(cursor.getColumnIndex("register_fee")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("source_status"))) {
            source.setSourceStatus(cursor.getInt(cursor.getColumnIndex("source_status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("version"))) {
            source.setVersion(cursor.getInt(cursor.getColumnIndex("version")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            source.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            source.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return source;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
