package com.example.smartminutes;

public class UsersInfo {

    private String username, email, password, uid;

    // Default constructor (needed for Firebase)
    public UsersInfo() {
    }

    // Constructor without UID (used before Firebase Authentication)
    public UsersInfo(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Constructor with UID (needed for Firebase Authentication)
    public UsersInfo(String username, String email, String password, String uid) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.uid = uid;  // ✅ Now properly storing UID
    }

    // Getter and Setter for Username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter for UID
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
