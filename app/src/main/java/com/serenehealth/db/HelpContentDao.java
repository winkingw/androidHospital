package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.HelpContent;

import java.util.ArrayList;
import java.util.List;

public class HelpContentDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public HelpContentDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 根据内容类型查询（如 help / privacy / flow），只查启用状态，按 sort_no 升序
     */
    public List<HelpContent> queryByType(String contentType) {
        List<HelpContent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_help_content WHERE content_type = ? AND status = 1 ORDER BY sort_no ASC",
                    new String[]{contentType});
            while (cursor.moveToNext()) {
                list.add(cursorToHelpContent(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询全部帮助内容，按 sort_no 升序
     */
    public List<HelpContent> queryAll() {
        List<HelpContent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_help_content ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToHelpContent(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 按 ID 查询
     */
    public HelpContent queryById(long id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_help_content WHERE id = ?",
                    new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                return cursorToHelpContent(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增帮助内容
     */
    public long insert(HelpContent helpContent) {
        ContentValues values = new ContentValues();
        values.put("content_type", helpContent.getContentType());
        if (helpContent.getTitle() != null) {
            values.put("title", helpContent.getTitle());
        }
        if (helpContent.getContent() != null) {
            values.put("content", helpContent.getContent());
        }
        if (helpContent.getVideoUrl() != null) {
            values.put("video_url", helpContent.getVideoUrl());
        }
        values.put("sort_no", helpContent.getSortNo());
        values.put("status", helpContent.getStatus());
        return db.insert("t_help_content", null, values);
    }

    /**
     * 更新帮助内容
     */
    public int update(HelpContent helpContent) {
        ContentValues values = new ContentValues();
        values.put("content_type", helpContent.getContentType());
        if (helpContent.getTitle() != null) {
            values.put("title", helpContent.getTitle());
        }
        if (helpContent.getContent() != null) {
            values.put("content", helpContent.getContent());
        }
        if (helpContent.getVideoUrl() != null) {
            values.put("video_url", helpContent.getVideoUrl());
        }
        values.put("sort_no", helpContent.getSortNo());
        values.put("status", helpContent.getStatus());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_help_content", values, "id = ?",
                new String[]{String.valueOf(helpContent.getId())});
    }

    /**
     * 删除帮助内容
     */
    public int delete(long id) {
        return db.delete("t_help_content", "id = ?",
                new String[]{String.valueOf(id)});
    }

    private HelpContent cursorToHelpContent(Cursor cursor) {
        HelpContent helpContent = new HelpContent();
        helpContent.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        helpContent.setContentType(cursor.getString(cursor.getColumnIndexOrThrow("content_type")));
        if (!cursor.isNull(cursor.getColumnIndex("title"))) {
            helpContent.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("content"))) {
            helpContent.setContent(cursor.getString(cursor.getColumnIndex("content")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("video_url"))) {
            helpContent.setVideoUrl(cursor.getString(cursor.getColumnIndex("video_url")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            helpContent.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("status"))) {
            helpContent.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            helpContent.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            helpContent.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return helpContent;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
