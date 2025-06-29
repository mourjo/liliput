package me.mourjo.lambda.handlers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import me.mourjo.cognito.TokenParser;
import me.mourjo.dto.LambdaResponse;
import me.mourjo.utils.ParameterStore;

public class ListLinksHandler implements RequestHandler<Map<String, ?>, LambdaResponse> {

    final Table table = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient()).getTable(
        ParameterStore.dynamoDbTableName());
    final Index userIndex = table.getIndex("user-index");
    final TokenParser tokenParser = new TokenParser();

    @Override
    public LambdaResponse handleRequest(Map<String, ?> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Raw input: " + input);
        Optional<String> email = tokenParser.emailFromHeaders(input);
        if (email.isPresent()) {
            return LambdaResponse.builder()
                .statusCode(200)
                .body(Map.of("userEmail", email.get(), "links", getLinks(email.get())))
                .cookies(tokenParser.refreshedCookies(input))
                .build();
        }

        return LambdaResponse.builder().statusCode(401).build();
    }

    List<Link> getLinks(String userEmail) {
        List<Link> items = new ArrayList<>();

        for (Item item : userIndex.query(new KeyAttribute("user", userEmail))) {
            items.add(Link.from(item));
        }
        items.sort(Comparator.comparingInt(Link::usageCount).reversed());
        return items.subList(0, Math.min(10, items.size()));
    }
}

record Link(String shortLink, String originalLink, int usageCount) {

    static Link from(Item item) {
        var shortLink = item.getString("key");
        var originalLink = item.getString("value");
        int usageCount = 0;
        if (item.hasAttribute("usageCount")) {
            usageCount = item.getInt("usageCount");
        }
        return new Link(shortLink, originalLink, usageCount);
    }
}
