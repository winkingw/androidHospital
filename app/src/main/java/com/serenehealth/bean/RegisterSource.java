package com.serenehealth.bean;

public class RegisterSource {
    private long id;
    private long scheduleId;
    private String slotStartTime;
    private String slotEndTime;
    private int totalNum;
    private int remainNum;
    private double registerFee;
    private int sourceStatus;
    private int version;
    private String createTime;
    private String updateTime;

    public RegisterSource() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(String slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public String getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(String slotEndTime) {
        this.slotEndTime = slotEndTime;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getRemainNum() {
        return remainNum;
    }

    public void setRemainNum(int remainNum) {
        this.remainNum = remainNum;
    }

    public double getRegisterFee() {
        return registerFee;
    }

    public void setRegisterFee(double registerFee) {
        this.registerFee = registerFee;
    }

    public int getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(int sourceStatus) {
        this.sourceStatus = sourceStatus;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
