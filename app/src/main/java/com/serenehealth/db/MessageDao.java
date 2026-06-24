package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public MessageDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询用户全部消息，按时间倒序
     */
    public List<Message> queryByUserId(long userId) {
        List<Message> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_message WHERE user_id = ? ORDER BY send_time DESC",
                    new String[]{String.valueOf(userId)});
            while (cursor.moveToNext()) {
                list.add(cursorToMessage(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询未读消息数
     */
    public int queryUnreadCount(long userId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM t_message WHERE user_id = ? AND is_read = 0",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 标记单条已读
     */
    public int markAsRead(long messageId) {
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        return db.update("t_message", values, "id = ?",
                new String[]{String.valueOf(messageId)});
    }

    /**
     * 全部标记已读
     */
    public int markAllAsRead(long userId) {
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        return db.update("t_message", values, "user_id = ? AND is_read = 0",
                new String[]{String.valueOf(userId)});
    }

    /**
     * 新增消息（系统触发时调用）
     */
    public long insert(Message message) {
        ContentValues values = new ContentValues();
        values.put("user_id", message.getUserId());
        values.put("title", message.getTitle());
        values.put("content", message.getContent());
        if (message.getBusinessType() != null) {
            values.put("business_type", message.getBusinessType());
        }
        if (message.getBusinessId() > 0) {
            values.put("business_id", message.getBusinessId());
        }
        values.put("is_read", message.getIsRead());
        if (message.getSendTime() != null) {
            values.put("send_time", message.getSendTime());
        }
        return db.insert("t_message", null, values);
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        message.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        if (!cursor.isNull(cursor.getColumnIndex("business_type"))) {
            message.setBusinessType(cursor.getString(cursor.getColumnIndex("business_type")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("business_id"))) {
            message.setBusinessId(cursor.getLong(cursor.getColumnIndex("business_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("is_read"))) {
            message.setIsRead(cursor.getInt(cursor.getColumnIndex("is_read")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("send_time"))) {
            message.setSendTime(cursor.getString(cursor.getColumnIndex("send_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            message.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        return message;
    }
}
