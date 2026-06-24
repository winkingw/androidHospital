package com.serenehealth.bean;

public class Feedback {
    private long id;
    private long userId;
    private long appointmentId;
    private long doctorId;
    private int doctorScore;
    private int serviceScore;
    private int visitScore;
    private String content;
    private String createTime;

    public Feedback() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public int getDoctorScore() {
        return doctorScore;
    }

    public void setDoctorScore(int doctorScore) {
        this.doctorScore = doctorScore;
    }

    public int getServiceScore() {
        return serviceScore;
    }

    public void setServiceScore(int serviceScore) {
        this.serviceScore = serviceScore;
    }

    public int getVisitScore() {
        return visitScore;
    }

    public void setVisitScore(int visitScore) {
        this.visitScore = visitScore;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
