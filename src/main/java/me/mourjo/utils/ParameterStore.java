package me.mourjo.utils;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

public class ParameterStore {

    private static ParameterStore instance;
    private final Map<String, String> cache = new HashMap<>();
    private final SsmClient ssmClient = SsmClient.builder()
        .region(Region.US_EAST_2)
        .build();

    private ParameterStore() {

    }

    private static ParameterStore getInstance() {
        if (instance == null) {
            instance = new ParameterStore();
        }
        return instance;
    }

    public static String cognitoUserPool() {
        return getInstance().getCached("LILIPUT_COGNITO_USER_POOL_ID");
    }

    public static String cognitoClientId() {
        return getInstance().getCached("LILIPUT_COGNITO_CLIENT_ID");
    }

    public static String cognitoRedirectUri() {
        return getInstance().getCached("LILIPUT_COGNITO_REDIRECT_URI");
    }

    public static String cognitoAuthEndpoint() {
        return getInstance().getCached("LILIPUT_COGNITO_AUTH_ENDPOINT");
    }

    public static String dynamoDbTableName() {
        return getInstance().getCached("LILIPUT_DDB_TABLE");
    }

    private String getCached(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        synchronized (cache) {
            var value = ssmClient.getParameter(builder -> builder.name(key)).parameter().value();
            cache.put(key, value);
            return value;
        }
    }
}
