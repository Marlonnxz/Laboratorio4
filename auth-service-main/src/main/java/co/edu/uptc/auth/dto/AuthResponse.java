package co.edu.uptc.auth.dto;

import co.edu.uptc.auth.entity.Role;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private String username;
    private String fullName;
    private Role role;
    private Long studentId;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresInMs, String username, String fullName, Role role, Long studentId) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.studentId = studentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
