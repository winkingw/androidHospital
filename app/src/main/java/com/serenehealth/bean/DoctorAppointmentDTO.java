package com.serenehealth.bean;

public class DoctorAppointmentDTO {
    private long id;
    private String appointmentNo;
    private long userId;
    private long sourceId;
    private String appointmentStatus;
    private String cancelReason;
    private String createTime;
    private String updateTime;

    private String patientName;
    private String patientPhone;
    private int patientGender;

    private String scheduleDate;
    private String period;
    private String clinicRoom;

    private String slotStartTime;
    private String slotEndTime;
    private double registerFee;

    private String doctorName;
    private String departmentName;

    public DoctorAppointmentDTO() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSourceId() { return sourceId; }
    public void setSourceId(long sourceId) { this.sourceId = sourceId; }

    public String getAppointmentStatus() { return appointmentStatus; }
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public int getPatientGender() { return patientGender; }
    public void setPatientGender(int patientGender) { this.patientGender = patientGender; }

    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getClinicRoom() { return clinicRoom; }
    public void setClinicRoom(String clinicRoom) { this.clinicRoom = clinicRoom; }

    public String getSlotStartTime() { return slotStartTime; }
    public void setSlotStartTime(String slotStartTime) { this.slotStartTime = slotStartTime; }

    public String getSlotEndTime() { return slotEndTime; }
    public void setSlotEndTime(String slotEndTime) { this.slotEndTime = slotEndTime; }

    public double getRegisterFee() { return registerFee; }
    public void setRegisterFee(double registerFee) { this.registerFee = registerFee; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
