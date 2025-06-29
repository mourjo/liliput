package me.mourjo.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;

@Builder
public record CognitoAuthTokenRequest(String redirectUri, String grantType, String clientId,
                                      String code, String codeVerifier, String refreshToken) {

    public String formEncodedBody() {
        List<String> bodyParts = new ArrayList<>();
        if (redirectUri != null) {
            bodyParts.add("redirect_uri=" + encode(redirectUri));
        }

        if (grantType != null) {
            bodyParts.add("grant_type=" + encode(grantType));
        }

        if (clientId != null) {
            bodyParts.add("client_id=" + encode(clientId));
        }

        if (code != null) {
            bodyParts.add("code=" + encode(code));
        }

        if (codeVerifier != null) {
            bodyParts.add("code_verifier=" + encode(codeVerifier));
        }

        if (refreshToken != null) {
            bodyParts.add("refresh_token=" + encode(refreshToken));
        }

        return String.join("&", bodyParts);
    }

    @Override
    public String toString() {
        return formEncodedBody();
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
