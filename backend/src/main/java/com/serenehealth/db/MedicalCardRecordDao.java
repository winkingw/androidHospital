package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.MedicalCardRecord;

import java.util.ArrayList;
import java.util.List;

public class MedicalCardRecordDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public MedicalCardRecordDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询某医保卡的消费记录，按时间倒序
     */
    public List<MedicalCardRecord> queryByCardId(long cardId) {
        List<MedicalCardRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_medical_card_record WHERE card_id = ? ORDER BY record_time DESC",
                    new String[]{String.valueOf(cardId)});
            while (cursor.moveToNext()) {
                list.add(cursorToMedicalCardRecord(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增消费记录
     */
    public long insert(MedicalCardRecord record) {
        ContentValues values = new ContentValues();
        values.put("card_id", record.getCardId());
        values.put("user_id", record.getUserId());
        if (record.getRelatedOrderId() > 0) {
            values.put("related_order_id", record.getRelatedOrderId());
        }
        values.put("record_type", record.getRecordType());
        values.put("amount", record.getAmount());
        if (record.getBalanceAfter() >= 0) {
            values.put("balance_after", record.getBalanceAfter());
        }
        if (record.getDescription() != null) {
            values.put("description", record.getDescription());
        }
        if (record.getRecordTime() != null) {
            values.put("record_time", record.getRecordTime());
        }
        return db.insert("t_medical_card_record", null, values);
    }

    private MedicalCardRecord cursorToMedicalCardRecord(Cursor cursor) {
        MedicalCardRecord record = new MedicalCardRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        record.setCardId(cursor.getLong(cursor.getColumnIndexOrThrow("card_id")));
        record.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        if (!cursor.isNull(cursor.getColumnIndex("related_order_id"))) {
            record.setRelatedOrderId(cursor.getLong(cursor.getColumnIndex("related_order_id")));
        }
        record.setRecordType(cursor.getString(cursor.getColumnIndexOrThrow("record_type")));
        if (!cursor.isNull(cursor.getColumnIndex("amount"))) {
            record.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("balance_after"))) {
            record.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("balance_after")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("description"))) {
            record.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("record_time"))) {
            record.setRecordTime(cursor.getString(cursor.getColumnIndex("record_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            record.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        return record;
    }
}
