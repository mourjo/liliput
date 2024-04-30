package me.mourjo.cognito;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import me.mourjo.utils.ParameterStore;
import software.amazon.awssdk.regions.Region;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class KeyProvider implements RSAKeyProvider {

    private final JwkProvider provider;
    private final String issuerUrl;

    public KeyProvider(String region, String userPoolId) {
        issuerUrl = "https://cognito-idp.%s.amazonaws.com/%s".formatted(region, userPoolId);

        // URL: https://cognito-idp.us-east-2.amazonaws.com/us-east-2_XXXX/.well-known/jwks.json
        // the "/.well-known/jwks.json" will be appended by JwkProviderBuilder
        provider = new JwkProviderBuilder(issuerUrl)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build();
    }

    String getIssuerUrl() {
        return issuerUrl;
    }

    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        try {
            return (RSAPublicKey) provider.get(kid).getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(String.format("Failed to get JWT kid=%s from tokenSigningKeyUrl=%s", kid, issuerUrl), e);
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}

public class CognitoTokenVerifier {

    private final JWTVerifier verifier;

    public CognitoTokenVerifier() {
        var keyProvider = new KeyProvider(Region.US_EAST_2.toString(), ParameterStore.cognitoUserPool());
        var algorithm = Algorithm.RSA256(keyProvider);
        verifier = JWT.require(algorithm)
                .withIssuer(keyProvider.getIssuerUrl())
                .build();
    }

    public boolean verify(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            System.err.println("Could not verify token:" + ex.getMessage());
            return false;
        }
    }

    public Optional<String> getEmail(String token) {
        try {
            if (token != null && !token.isBlank()) {
                var decodedJwt = verifier.verify(token);
                if (decodedJwt.getClaims().containsKey("email")) {
                    return Optional.of(decodedJwt.getClaims().get("email").asString());
                }
            }
        } catch (JWTDecodeException e) {

        } catch (JWTVerificationException ex) {

        }

        return Optional.empty();
    }
}
