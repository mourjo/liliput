package me.mourjo.lambda.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import me.mourjo.dto.LambdaResponse;
import me.mourjo.utils.ParameterStore;

import java.util.Map;

public class ExpandLinkHandler implements RequestHandler<Map<String, ?>, LambdaResponse> {

    final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
    final DynamoDB dynamoDB = new DynamoDB(ddb);
    final Table table = dynamoDB.getTable(ParameterStore.dynamoDbTableName());

    @Override
    public LambdaResponse handleRequest(Map<String, ?> input, Context context) {
        if (input.containsKey("path") && input.get("path") != null && input.get("path") instanceof String) {
            String[] shortLinkParts = ((String) input.get("path")).split("/");
            String shortLink = shortLinkParts[shortLinkParts.length - 1];

            if (shortLink != null) {
                Item item = table.getItem(new PrimaryKey("key", shortLink));
                if (item != null) {
                    String originalLink = item.getString("value");
                    updateUsageCount(shortLink);
                    return LambdaResponse.builder().statusCode(302).locationHeader(originalLink).build();
                }
            }
        }

        return LambdaResponse.builder().statusCode(404).body(Map.of("message", "Not found")).build();
    }

    void updateUsageCount(String shortLink) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("key", shortLink)
                .withUpdateExpression("SET #countV = if_not_exists(#countV, :defaultVal) + :val")
                .withNameMap(new NameMap().with("#countV", "usageCount"))
                .withValueMap(new ValueMap().withNumber(":val", 1).withNumber(":defaultVal", 0));
        table.updateItem(updateItemSpec);
    }
}
