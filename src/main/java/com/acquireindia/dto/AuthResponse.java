package com.acquireindia.dto;

import com.acquireindia.model.User;

public class AuthResponse {
    private User user;
    private String token;
    private String type = "Bearer";

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}