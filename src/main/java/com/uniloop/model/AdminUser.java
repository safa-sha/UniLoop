package com.uniloop.model;

public class AdminUser {

    private final int userId;
    private final String name;
    private final String email;
    private final String role;
    private final String status;
    private final String registeredAt;

    public AdminUser(int userId, String name, String email, String role, String status,
                     String registeredAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getRegisteredAt() { return registeredAt; }
}
