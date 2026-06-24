package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Banner;

import java.util.ArrayList;
import java.util.List;

public class BannerDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public BannerDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询当前有效的轮播图，按sort_no升序
     */
    public List<Banner> queryActiveBanners() {
        List<Banner> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_banner WHERE status = 1 "
                            + "AND (start_time IS NULL OR start_time <= datetime('now','localtime')) "
                            + "AND (end_time IS NULL OR end_time >= datetime('now','localtime')) "
                            + "ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToBanner(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 管理员查全部
     */
    public List<Banner> queryAllBanners() {
        List<Banner> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_banner ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToBanner(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增轮播图
     */
    public long insert(Banner banner) {
        ContentValues values = new ContentValues();
        if (banner.getTitle() != null) {
            values.put("title", banner.getTitle());
        }
        values.put("image_url", banner.getImageUrl());
        if (banner.getJumpType() != null) {
            values.put("jump_type", banner.getJumpType());
        }
        if (banner.getJumpValue() != null) {
            values.put("jump_value", banner.getJumpValue());
        }
        values.put("sort_no", banner.getSortNo());
        values.put("status", banner.getStatus());
        if (banner.getStartTime() != null) {
            values.put("start_time", banner.getStartTime());
        }
        if (banner.getEndTime() != null) {
            values.put("end_time", banner.getEndTime());
        }
        return db.insert("t_banner", null, values);
    }

    /**
     * 更新轮播图
     */
    public int update(Banner banner) {
        ContentValues values = new ContentValues();
        if (banner.getTitle() != null) {
            values.put("title", banner.getTitle());
        }
        values.put("image_url", banner.getImageUrl());
        if (banner.getJumpType() != null) {
            values.put("jump_type", banner.getJumpType());
        }
        if (banner.getJumpValue() != null) {
            values.put("jump_value", banner.getJumpValue());
        }
        values.put("sort_no", banner.getSortNo());
        values.put("status", banner.getStatus());
        if (banner.getStartTime() != null) {
            values.put("start_time", banner.getStartTime());
        }
        if (banner.getEndTime() != null) {
            values.put("end_time", banner.getEndTime());
        }
        values.put("update_time", getCurrentDateTime());
        return db.update("t_banner", values, "id = ?",
                new String[]{String.valueOf(banner.getId())});
    }

    /**
     * 删除轮播图
     */
    public int delete(long bannerId) {
        return db.delete("t_banner", "id = ?",
                new String[]{String.valueOf(bannerId)});
    }

    private Banner cursorToBanner(Cursor cursor) {
        Banner banner = new Banner();
        banner.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        if (!cursor.isNull(cursor.getColumnIndex("title"))) {
            banner.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        }
        banner.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
        if (!cursor.isNull(cursor.getColumnIndex("jump_type"))) {
            banner.setJumpType(cursor.getString(cursor.getColumnIndex("jump_type")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("jump_value"))) {
            banner.setJumpValue(cursor.getString(cursor.getColumnIndex("jump_value")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            banner.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("status"))) {
            banner.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("start_time"))) {
            banner.setStartTime(cursor.getString(cursor.getColumnIndex("start_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("end_time"))) {
            banner.setEndTime(cursor.getString(cursor.getColumnIndex("end_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            banner.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            banner.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return banner;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
