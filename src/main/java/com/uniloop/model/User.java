package com.uniloop.model;
public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String status;
    public User(String name, String email, String passwordHash) { this.name = name; this.email = email; this.passwordHash = passwordHash; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
