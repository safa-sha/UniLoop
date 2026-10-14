package com.uniloop.model;
public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String status;
    private String phone;
    private String department;
    private Integer admissionYear;
    private String courseProgram;

    public User(String name, String email, String passwordHash) { this.name = name; this.email = email; this.passwordHash = passwordHash; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
    public String getCourseProgram() { return courseProgram; }
    public void setCourseProgram(String courseProgram) { this.courseProgram = courseProgram; }
}
