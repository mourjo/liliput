package me.mourjo.lambda.handlers;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import me.mourjo.cognito.CognitoTokenVerifier;
import me.mourjo.utils.CookieUtils;

public class CookieAuthorizer implements RequestHandler<Map<String, ?>, Map<String, ?>> {
    // Input:
    // {"type":"TOKEN","methodArn":"arn:aws:execute-api:us-east-2:XXXXX/XXX-stage/GET/","authorizationToken":"xxx"}

    private final CognitoTokenVerifier verifier = new CognitoTokenVerifier();

    @Override
    public Map<String, ?> handleRequest(Map<String, ?> event, Context context) {
        final LambdaLogger logger = context.getLogger();

        if (event != null && event.containsKey("authorizationToken") && event.containsKey(
            "methodArn")) {
            String token = (String) event.get("authorizationToken");
            String methodArn = (String) event.get("methodArn");
            try {
                var accessToken = CookieUtils.findCookieByName(token, "access_token");
                if (accessToken.isPresent()) {
                    if (verifier.verify(accessToken.get())) {
                        var idToken = CookieUtils.findCookieByName(token, "id_token");
                        if (idToken.isPresent()) {
                            var emailFromIdToken = verifier.getEmail(idToken.get());
                            if (emailFromIdToken.isPresent()) {
                                return allow(emailFromIdToken.get(), methodArn);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.log("Error: " + e.getMessage());
            }
            return deny(methodArn);
        }
        return deny("Unknown");
    }

    Map<String, ?> allow(String principal, String methodArn) {
        return policy("Allow", principal, methodArn);
    }

    Map<String, ?> deny(String methodArn) {
        return policy("Deny", "user", methodArn);
    }

    Map<String, ?> policy(String effect, String principal, String resource) {
        if (effect != null && resource != null) {
            var statement = Map.of(
                "Action", "execute-api:Invoke",
                "Effect", effect,
                "Resource", resource
            );

            var policyDocument = Map.of(
                "Version", "2012-10-17",
                "Statement", List.of(statement));

            return Map.of(
                "principalId", principal,
                "policyDocument", policyDocument
            );
        }

        return Map.of("principalId", principal);
    }
}
