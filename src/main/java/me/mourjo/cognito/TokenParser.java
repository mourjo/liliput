package me.mourjo.cognito;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.mourjo.dto.Cookie;
import me.mourjo.utils.CookieUtils;

public class TokenParser {

    private final CognitoTokenVerifier verifier = new CognitoTokenVerifier();
    private final AuthExchanger exchanger = new AuthExchanger();

    public Optional<String> emailFromHeaders(Map<String, ?> rawInput) {

        // principal sent by authorizer in requestContext
        if (rawInput.containsKey("requestContext") && rawInput.get(
            "requestContext") instanceof Map) {
            Map<String, ?> requestContext = (Map<String, ?>) rawInput.get("requestContext");
            if (requestContext.containsKey("authorizer") && requestContext.get(
                "authorizer") instanceof Map) {
                Map<String, ?> authorizer = (Map<String, ?>) requestContext.get("authorizer");
                if (authorizer.get("principalId") != null) {
                    return Optional.of((String) authorizer.get("principalId"));
                }
            }
        }

        // principal not found in authorizer response - try reading the raw cookies
        if (rawInput.containsKey("headers")) {
            String cookieHeader = ((Map<String, String>) rawInput.get("headers")).get("cookie");
            if (cookieHeader != null) {
                var idToken = CookieUtils.findCookieByName(cookieHeader, "id_token");
                if (idToken.isPresent()) {
                    return verifier.getEmail(idToken.get());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<List<Cookie>> refreshedCookies(Map<String, ?> rawInput) {
        if (rawInput.containsKey("headers")) {
            String cookieHeader = ((Map<String, String>) rawInput.get("headers")).get("cookie");
            if (cookieHeader != null) {
                var refreshToken = CookieUtils.findCookieByName(cookieHeader, "refresh_token");
                if (refreshToken.isPresent()) {
                    // Note: The refresh token is encrypted and not verifiable via a signature
                    try {
                        var cognitoResponse = exchanger.refreshAccessTokens(refreshToken.get());
                        if (!cognitoResponse.hasError()) {
                            var accessTokenCookie = Cookie.builder()
                                .name("access_token")
                                .value(cognitoResponse.accessToken())
                                .maxAge(Duration.ofHours(1).toSeconds())
                                .sameSite(Cookie.SameSite.STRICT)
                                .build();

                            var idTokenCookie = Cookie.builder()
                                .name("id_token")
                                .value(cognitoResponse.idToken())
                                .maxAge(Duration.ofHours(1).toSeconds())
                                .sameSite(Cookie.SameSite.STRICT)
                                .build();

                            return Optional.of(List.of(accessTokenCookie, idTokenCookie));
                        }

                    } catch (IOException | InterruptedException e) {
                        System.err.println("Failed to refresh token: " + e.getMessage());
                    }
                }
            }
        }

        System.err.println("Could not refresh cookies");
        return Optional.empty();
    }

}
