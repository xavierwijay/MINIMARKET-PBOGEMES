package fafbar.model;

import java.sql.Timestamp;

public class User {

    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private String email;
    private boolean isActive;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String username, String password, String fullName,
                String role, String email, boolean isActive, Timestamp createdAt) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // =====================
    // GETTERS
    // =====================
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // =====================
    // SETTERS
    // =====================
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}