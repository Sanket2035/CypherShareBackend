package com.cyphershare.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String tier;

    public AuthResponse(String token, String email, String tier) {
        this.token = token;
        this.email = email;
        this.tier = tier;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getTier() { return tier; }
}
