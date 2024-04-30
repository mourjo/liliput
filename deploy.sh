#!/usr/bin/env bash

mvn clean package;

aws s3 cp src/main/resources/index.html s3://liliput-resources/index.html ;
aws s3 cp target/liliput-1.0-SNAPSHOT.jar  s3://liliput-resources/liliput-1.0-SNAPSHOT.jar ;

for functionName in $(aws lambda list-functions --query "Functions[?Runtime == 'java21'].FunctionName " --no-cli-pager --region us-east-2 | jq -r '.[]'); do
  echo "Deploying $functionName";
  # aws lambda update-function-configuration --function-name "$functionName" --no-cli-pager --environment "Variables={JAVA_TOOL_OPTIONS=-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseParallelGC}" | jq .;
  aws lambda update-function-code --function-name "$functionName" --s3-bucket liliput-resources --s3-key liliput-1.0-SNAPSHOT.jar --publish --no-cli-pager | jq .;
done;

