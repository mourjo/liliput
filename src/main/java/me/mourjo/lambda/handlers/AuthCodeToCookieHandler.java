package me.mourjo.lambda.handlers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import me.mourjo.cognito.AuthExchanger;
import me.mourjo.cognito.CognitoTokenVerifier;
import me.mourjo.cognito.TokenParser;
import me.mourjo.dto.AuthCodeToCookieParams;
import me.mourjo.dto.CognitoAuthResponse;
import me.mourjo.dto.Cookie;
import me.mourjo.dto.LambdaResponse;

public class AuthCodeToCookieHandler implements RequestHandler<Map<String, ?>, LambdaResponse> {

    private final CognitoTokenVerifier verifier = new CognitoTokenVerifier();

    private final TokenParser tokenParser = new TokenParser();
    private final AuthExchanger exchanger = new AuthExchanger();

    @Override
    public LambdaResponse handleRequest(Map<String, ?> input, Context context) {
        final LambdaLogger logger = context.getLogger();

        Optional<String> emailFromCookies = tokenParser.emailFromHeaders(input);
        if (emailFromCookies.isPresent()) {
            return LambdaResponse.builder()
                .body(Map.of("userEmail", emailFromCookies.get()))
                .build();
        }

        try {
            Optional<AuthCodeToCookieParams> postBody = parseBody(logger, input);
            if (postBody.isPresent()) {
                AuthCodeToCookieParams authParams = postBody.get();
                Optional<String> code = readCode(logger, authParams);
                Optional<String> codeVerifier = readCodeVerifier(logger, authParams);
                if (code.isPresent() && codeVerifier.isPresent()) {
                    var tokens = exchanger.exchangeAuthCodeForTokens(code.get(),
                        codeVerifier.get());
                    if (!tokens.hasError()) {
                        var cookies = cookiesFromTokens(tokens);
                        var email = verifier.getEmail(tokens.id_token());
                        if (email.isPresent()) {
                            return LambdaResponse.builder()
                                .cookies(cookies)
                                .body(Map.of("userEmail", email.get()))
                                .build();
                        } else {
                            return LambdaResponse.builder().statusCode(400)
                                .body(Map.of("userEmail", "No email!")).build();
                        }
                    }
                    return LambdaResponse.builder().statusCode(400)
                        .body(Map.of("message", "Invalid Code")).build();

                }
            }
        } catch (JsonProcessingException error) {
            logger.log("Error in json processing: " + error.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.log(e.getMessage());
        }

        return LambdaResponse.builder().statusCode(400)
            .body(Map.of("message", "Something went wrong!")).build();
    }

    private List<Cookie> cookiesFromTokens(CognitoAuthResponse tokens) {
        var accessTokenCookie = Cookie.builder()
            .name("access_token")
            .value(tokens.accessToken())
            .maxAge(Duration.ofHours(1).toSeconds())
            .sameSite(Cookie.SameSite.STRICT)
            .build();

        var idTokenCookie = Cookie.builder()
            .name("id_token")
            .value(tokens.idToken())
            .maxAge(Duration.ofHours(1).toSeconds())
            .sameSite(Cookie.SameSite.STRICT)
            .build();

        var refreshTokenCookie = Cookie.builder()
            .name("refresh_token")
            .value(tokens.refreshToken())
            .maxAge(Duration.ofDays(30).toSeconds())
            .sameSite(Cookie.SameSite.STRICT)
            .build();

        return List.of(accessTokenCookie, refreshTokenCookie, idTokenCookie);
    }

    private Optional<AuthCodeToCookieParams> parseBody(LambdaLogger logger, Map<String, ?> input)
        throws JsonProcessingException {
        if (input == null || input.isEmpty()) {
            logger.log("Body not present");
            return Optional.empty();
        }

        if (!input.containsKey("body") || input.get("body") == null) {
            logger.log("Body not present");
            return Optional.empty();
        }

        var payload = (String) input.get("body");

        var params = Jackson.getObjectMapper().readValue(payload, AuthCodeToCookieParams.class);
        return Optional.of(params);

    }

    private Optional<String> readCode(LambdaLogger logger, AuthCodeToCookieParams params)
        throws JsonProcessingException {
        if (params.code() == null) {
            logger.log("Code not passed in body");
            return Optional.empty();
        }

        logger.log("Successfully read code: " + params.code());
        return Optional.of(params.code());
    }

    private Optional<String> readCodeVerifier(LambdaLogger logger, AuthCodeToCookieParams params)
        throws JsonProcessingException {
        if (params.codeVerifier() == null) {
            logger.log("Code verifier not passed in body");
            return Optional.empty();
        }
        logger.log("Successfully read code: " + params.codeVerifier());
        return Optional.of(params.codeVerifier());
    }
}
