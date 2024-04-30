package me.mourjo.lambda.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JacksonException;
import me.mourjo.cognito.TokenParser;
import me.mourjo.dto.LambdaResponse;
import me.mourjo.utils.ParameterStore;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static me.mourjo.utils.RandomStringGenerator.generateRandomString;

public class CreateLinkHandler implements RequestHandler<Map<String, ?>, LambdaResponse> {

    private final static String COUNTER_KEY = "__global_link_counter__";
    private final TokenParser tokenParser = new TokenParser();
    private final Table table = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient()).getTable(ParameterStore.dynamoDbTableName());
    private final Index valueIndex = table.getIndex("value-index");

    boolean isValidLink(String link) {
        try {
            String protocol = new URL(link).getProtocol();
            return "https".equals(protocol) || "http".equals(protocol);
        } catch (MalformedURLException e) {
        }
        return false;
    }

    @Override
    public LambdaResponse handleRequest(Map<String, ?> input, Context context) {
        Optional<String> email = tokenParser.emailFromHeaders(input);

        try {
            if (email.isPresent() && input.containsKey("body")) {
                Map<String, String> body = Jackson.getObjectMapper().readValue((String) input.get("body"), Map.class);

                if (body.containsKey("link") && isValidLink(body.get("link"))) {
                    String linkToShorten = body.get("link");
                    var existingLink = findExistingLink(linkToShorten, email.get());
                    if (existingLink.isPresent()) {
                        return LambdaResponse.builder()
                                .statusCode(200)
                                .body(Map.of("shortLink", existingLink.get(), "userEmail", email.get()))
                                .cookies(tokenParser.refreshedCookies(input))
                                .build();
                    }

                    String shortLink = generateRandomString(4) + nextId();

                    Item item = new Item()
                            .withPrimaryKey("key", shortLink, "user", email.get())
                            .withString("value", linkToShorten)
                            .withInt("usageCount", 0)
                            .withString("createdAt", Instant.now().toString());

                    table.putItem(item);

                    return LambdaResponse.builder()
                            .statusCode(200)
                            .body(Map.of("shortLink", shortLink, "userEmail", email.get()))
                            .cookies(tokenParser.refreshedCookies(input))
                            .build();
                }
            }
        } catch (JacksonException e) {
            context.getLogger().log("Problem in parsing body payload");
        }

        return LambdaResponse.builder().statusCode(400).build();
    }

    Optional<String> findExistingLink(String originalLink, String email) {
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("#valueCol = :value")
                .withFilterExpression("#userCol = :user")
                .withNameMap(new NameMap().with("#valueCol", "value").with("#userCol", "user"))
                .withValueMap(new ValueMap().withString(":value", originalLink).withString(":user", email));

        ItemCollection<QueryOutcome> items = valueIndex.query(spec);

        for (Item item : items) {
            var shortLink = item.getString("key");
            if (shortLink != null) {
                return Optional.of(shortLink);
            }
        }
        return Optional.empty();
    }

    String nextId() {
        UpdateItemSpec spec = new UpdateItemSpec()
                .withPrimaryKey("key", COUNTER_KEY)
                .withUpdateExpression("SET #countV = #countV + :val")
                .withNameMap(new NameMap().with("#countV", "count"))
                .withValueMap(new ValueMap().withNumber(":val", 1))
                .withReturnValues(ReturnValue.UPDATED_NEW);
        long id = table.updateItem(spec).getItem().getLong("count");
        return Long.toString(id, Character.MAX_RADIX);
    }
}
