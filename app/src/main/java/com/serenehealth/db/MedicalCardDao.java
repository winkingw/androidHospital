package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.MedicalCard;

public class MedicalCardDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public MedicalCardDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询用户绑定的医保卡，未绑定返回null
     */
    public MedicalCard queryByUserId(long userId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_medical_card WHERE user_id = ? AND bind_status = 'BOUND'",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                return cursorToMedicalCard(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 绑定医保卡，返回记录ID
     */
    public long bind(MedicalCard card) {
        ContentValues values = new ContentValues();
        values.put("user_id", card.getUserId());
        values.put("holder_name", card.getHolderName());
        values.put("id_card_no", card.getIdCardNo());
        values.put("medical_card_no", card.getMedicalCardNo());
        values.put("bind_phone", card.getBindPhone());
        values.put("balance", card.getBalance());
        values.put("valid_start", card.getValidStart());
        values.put("valid_end", card.getValidEnd());
        values.put("bind_status", card.getBindStatus());
        values.put("unbind_time", (String) null);
        values.put("update_time", getCurrentDateTime());
        if (hasCardRow(card.getUserId())) {
            int rows = db.update("t_medical_card", values, "user_id = ?",
                    new String[]{String.valueOf(card.getUserId())});
            return rows > 0 ? card.getUserId() : -1;
        }
        return db.insert("t_medical_card", null, values);
    }

    /**
     * 解绑（bind_status→UNBOUND）
     */
    public int unbind(long userId) {
        ContentValues values = new ContentValues();
        values.put("bind_status", "UNBOUND");
        values.put("unbind_time", getCurrentDateTime());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_medical_card", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    /**
     * 更新余额
     */
    public int updateBalance(long userId, double newBalance) {
        ContentValues values = new ContentValues();
        values.put("balance", newBalance);
        values.put("update_time", getCurrentDateTime());
        return db.update("t_medical_card", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    private MedicalCard cursorToMedicalCard(Cursor cursor) {
        MedicalCard card = new MedicalCard();
        card.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        card.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        card.setHolderName(cursor.getString(cursor.getColumnIndexOrThrow("holder_name")));
        card.setIdCardNo(cursor.getString(cursor.getColumnIndexOrThrow("id_card_no")));
        card.setMedicalCardNo(cursor.getString(cursor.getColumnIndexOrThrow("medical_card_no")));
        card.setBindPhone(cursor.getString(cursor.getColumnIndexOrThrow("bind_phone")));
        if (!cursor.isNull(cursor.getColumnIndex("balance"))) {
            card.setBalance(cursor.getDouble(cursor.getColumnIndex("balance")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("valid_start"))) {
            card.setValidStart(cursor.getString(cursor.getColumnIndex("valid_start")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("valid_end"))) {
            card.setValidEnd(cursor.getString(cursor.getColumnIndex("valid_end")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("bind_status"))) {
            card.setBindStatus(cursor.getString(cursor.getColumnIndex("bind_status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("bind_time"))) {
            card.setBindTime(cursor.getString(cursor.getColumnIndex("bind_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("unbind_time"))) {
            card.setUnbindTime(cursor.getString(cursor.getColumnIndex("unbind_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            card.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            card.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return card;
    }

    private boolean hasCardRow(long userId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id FROM t_medical_card WHERE user_id = ?",
                    new String[]{String.valueOf(userId)});
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
