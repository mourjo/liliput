package me.mourjo.dto;

public record CognitoAuthResponse(String id_token, String access_token, String refresh_token, String error) {

    public String idToken() {
        return id_token;
    }

    public String accessToken() {
        return access_token;
    }

    public String refreshToken() {
        return refresh_token;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty() && !error.isBlank();
    }
}
