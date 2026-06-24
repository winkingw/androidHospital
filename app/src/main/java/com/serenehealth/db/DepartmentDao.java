package com.serenehealth.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serenehealth.bean.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public DepartmentDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询全部科室，按sort_no升序
     */
    public List<Department> queryAllDepartments() {
        List<Department> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_department ORDER BY sort_no ASC", null);
            while (cursor.moveToNext()) {
                list.add(cursorToDepartment(cursor));
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据ID查科室
     */
    public Department queryDepartmentById(long deptId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM t_department WHERE id = ?",
                    new String[]{String.valueOf(deptId)});
            if (cursor.moveToFirst()) {
                return cursorToDepartment(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 新增科室，返回新ID
     */
    public long insertDepartment(Department dept) {
        ContentValues values = new ContentValues();
        values.put("dept_name", dept.getDeptName());
        if (dept.getDeptCode() != null) {
            values.put("dept_code", dept.getDeptCode());
        }
        values.put("parent_id", dept.getParentId());
        if (dept.getDescription() != null) {
            values.put("description", dept.getDescription());
        }
        values.put("sort_no", dept.getSortNo());
        return db.insert("t_department", null, values);
    }

    /**
     * 更新科室
     */
    public int updateDepartment(Department dept) {
        ContentValues values = new ContentValues();
        values.put("dept_name", dept.getDeptName());
        if (dept.getDeptCode() != null) {
            values.put("dept_code", dept.getDeptCode());
        }
        values.put("parent_id", dept.getParentId());
        if (dept.getDescription() != null) {
            values.put("description", dept.getDescription());
        }
        values.put("sort_no", dept.getSortNo());
        values.put("update_time", getCurrentDateTime());
        return db.update("t_department", values, "id = ?",
                new String[]{String.valueOf(dept.getId())});
    }

    /**
     * 删除科室（需检查是否有医生关联）
     */
    public int deleteDepartment(long deptId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM t_doctor WHERE department_id = ?",
                    new String[]{String.valueOf(deptId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return db.delete("t_department", "id = ?",
                new String[]{String.valueOf(deptId)});
    }

    private Department cursorToDepartment(Cursor cursor) {
        Department dept = new Department();
        dept.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        dept.setDeptName(cursor.getString(cursor.getColumnIndexOrThrow("dept_name")));
        if (!cursor.isNull(cursor.getColumnIndex("dept_code"))) {
            dept.setDeptCode(cursor.getString(cursor.getColumnIndex("dept_code")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("parent_id"))) {
            dept.setParentId(cursor.getLong(cursor.getColumnIndex("parent_id")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("description"))) {
            dept.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("sort_no"))) {
            dept.setSortNo(cursor.getInt(cursor.getColumnIndex("sort_no")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("create_time"))) {
            dept.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        }
        if (!cursor.isNull(cursor.getColumnIndex("update_time"))) {
            dept.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
        }
        return dept;
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
