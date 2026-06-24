package com.serenehealth.bean;

public class SymptomDepartmentRule {
    private long id;
    private String symptomKeyword;
    private long departmentId;
    private String recommendReason;
    private int sortNo;
    private int status;
    private String createTime;

    public SymptomDepartmentRule() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSymptomKeyword() {
        return symptomKeyword;
    }

    public void setSymptomKeyword(String symptomKeyword) {
        this.symptomKeyword = symptomKeyword;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public String getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }

    public int getSortNo() {
        return sortNo;
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
