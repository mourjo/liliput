package me.mourjo.cognito;

import com.amazonaws.util.json.Jackson;
import me.mourjo.dto.CognitoAuthResponse;
import me.mourjo.dto.CognitoAuthTokenRequest;
import me.mourjo.utils.ParameterStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthExchanger {

    private final HttpClient httpClient;
    private final URI AUTH_ENDPOINT = URI.create(ParameterStore.cognitoAuthEndpoint());

    public AuthExchanger() {
        httpClient = HttpClient.newHttpClient();
    }

    public CognitoAuthResponse exchangeAuthCodeForTokens(String code, String codeVerifier) throws IOException, InterruptedException {
        var params = CognitoAuthTokenRequest.builder()
                .codeVerifier(codeVerifier)
                .code(code)
                .clientId(ParameterStore.cognitoClientId())
                .grantType("authorization_code")
                .redirectUri(ParameterStore.cognitoRedirectUri())
                .build();

        return post(params);
    }

    public CognitoAuthResponse refreshAccessTokens(String refreshToken) throws IOException, InterruptedException {
        var params = CognitoAuthTokenRequest.builder()
                .clientId(ParameterStore.cognitoClientId())
                .grantType("refresh_token")
                .refreshToken(refreshToken)
                .build();

        return post(params);
    }

    private CognitoAuthResponse post(CognitoAuthTokenRequest parameters) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(AUTH_ENDPOINT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(parameters.formEncodedBody()))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var responseString = response.body();

        return Jackson.getObjectMapper().readValue(responseString, CognitoAuthResponse.class);
    }

}
