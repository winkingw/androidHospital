package com.serenehealth.bean;

public class User {
    private long id;
    private String phone;
    private String password;
    private String realName;
    private int gender;
    private String birthDate;
    private String idCardNo;
    private String idCardFrontUri;
    private String idCardBackUri;
    private int realNameVerified;
    private int healthScore;
    private String memberLevel;
    private String createTime;
    private String updateTime;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getIdCardFrontUri() {
        return idCardFrontUri;
    }

    public void setIdCardFrontUri(String idCardFrontUri) {
        this.idCardFrontUri = idCardFrontUri;
    }

    public String getIdCardBackUri() {
        return idCardBackUri;
    }

    public void setIdCardBackUri(String idCardBackUri) {
        this.idCardBackUri = idCardBackUri;
    }

    public int getRealNameVerified() {
        return realNameVerified;
    }

    public void setRealNameVerified(int realNameVerified) {
        this.realNameVerified = realNameVerified;
    }

    public boolean isRealNameVerified() {
        return realNameVerified == 1;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
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
