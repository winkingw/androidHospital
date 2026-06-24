package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.SymptomDepartmentRule;

import java.util.ArrayList;
import java.util.List;

public class SymptomDepartmentRuleDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public SymptomDepartmentRuleDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 根据症状关键词模糊匹配科室列表，支持多关键词（按空格/逗号/中文逗号拆分），
     * 多关键词之间为 OR 关系。按sort_no升序，只返回status=1的有效规则
     */
    public List<SymptomDepartmentRule> queryBySymptom(String keyword) {
        List<SymptomDepartmentRule> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return list;
        }

        // 按空格、逗号、中文逗号拆分关键词
        String[] keywords = keyword.split("[\\s,，]+");

        // 构建 OR 查询条件
        StringBuilder whereClause = new StringBuilder();
        List<String> argsList = new ArrayList<>();
        for (String kw : keywords) {
            String trimmed = kw.trim();
            if (trimmed.isEmpty()) continue;
            if (whereClause.length() > 0) {
                whereClause.append(" OR ");
            }
            whereClause.append("symptom_keyword LIKE ?");
            argsList.add("%" + trimmed + "%");
        }

        if (whereClause.length() == 0) {
            return list;
        }

        String sql = "SELECT * FROM t_symptom_department_rule "
                + "WHERE (" + whereClause + ") AND status = 1 "
                + "ORDER BY sort_no ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, argsList.toArray(new String[0]));
            while (cursor.moveToNext()) {
                list.add(cursorToRule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询全部规则，按sort_no排序
     */
    public List<SymptomDepartmentRule> queryAll() {
        List<SymptomDepartmentRule> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_symptom_department_rule ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToRule(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 按ID查询
     */
    public SymptomDepartmentRule queryById(long id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_symptom_department_rule WHERE id = ?",
                    new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                return cursorToRule(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 插入规则，返回新ID
     */
    public long insert(SymptomDepartmentRule rule) {
        ContentValues values = new ContentValues();
        values.put("symptom_keyword", rule.getSymptomKeyword());
        values.put("department_id", rule.getDepartmentId());
        if (rule.getRecommendReason() != null) {
            values.put("recommend_reason", rule.getRecommendReason());
        }
        values.put("sort_no", rule.getSortNo());
        values.put("status", rule.getStatus());
        values.put("create_time", getCurrentDateTime());
        return db.insert("t_symptom_department_rule", null, values);
    }

    /**
     * 更新规则
     */
    public int update(SymptomDepartmentRule rule) {
        ContentValues values = new ContentValues();
        values.put("symptom_keyword", rule.getSymptomKeyword());
        values.put("department_id", rule.getDepartmentId());
        if (rule.getRecommendReason() != null) {
            values.put("recommend_reason", rule.getRecommendReason());
        }
        values.put("sort_no", rule.getSortNo());
        values.put("status", rule.getStatus());
        return db.update("t_symptom_department_rule", values, "id = ?",
                new String[]{String.valueOf(rule.getId())});
    }

    /**
     * 删除规则
     */
    public int delete(long id) {
        return db.delete("t_symptom_department_rule", "id = ?",
                new String[]{String.valueOf(id)});
    }

    private SymptomDepartmentRule cursorToRule(Cursor cursor) {
        SymptomDepartmentRule rule = new SymptomDepartmentRule();
        rule.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        rule.setSymptomKeyword(cursor.getString(cursor.getColumnIndexOrThrow("symptom_keyword")));
        rule.setDepartmentId(cursor.getLong(cursor.getColumnIndexOrThrow("department_id")));
        if (!cursor.isNull(cursor.getColumnIndex("recommend_reason"))) {
            rule.setRecommendReason(cursor.getString(cursor.getColumnIndex("recommend_reason")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            rule.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("status"))) {
            rule.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            rule.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        return rule;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
