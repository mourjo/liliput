package me.mourjo.dto;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record LambdaResponse(
        int statusCode,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, List<String>> multiValueHeaders,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String body) {

    public static LambdaResponseBuilder builder() {
        return new LambdaResponseBuilder();
    }

    public static class LambdaResponseBuilder {

        List<Cookie> cookies = List.of();
        int statusCode = 200;
        Map<String, ?> body;

        Map<String, String> singleValueHeaders = new HashMap<>();

        public LambdaResponseBuilder() {
        }

        private static String asJson(Map<String, ?> b) {
            try {
                if (b != null && !b.isEmpty()) {
                    return Jackson.getObjectMapper().writeValueAsString(b);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        public LambdaResponseBuilder cookies(List<Cookie> cookies) {
            this.cookies = cookies;
            return this;
        }

        public LambdaResponseBuilder locationHeader(String location) {
            singleValueHeaders.put("Location", location);
            return this;
        }

        public LambdaResponseBuilder cookies(Optional<List<Cookie>> updatedCookies) {
            if (updatedCookies.isPresent()) {
                this.cookies = updatedCookies.get();
            }
            return this;
        }

        public LambdaResponseBuilder body(Map<String, ?> body) {
            if (body != null) {
                this.body = body;
            }
            return this;
        }

        public LambdaResponseBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public LambdaResponse build() {
            Map<String, List<String>> multiValueHeaders = new HashMap<>();
            if (!cookies.isEmpty()) {
                multiValueHeaders.put("Set-Cookie", cookies.stream().map(Object::toString).toList());
            }

            if (!singleValueHeaders.isEmpty()) {
                for (String header : singleValueHeaders.keySet()) {
                    multiValueHeaders.put(header, List.of(singleValueHeaders.get(header)));
                }
            }

            if (multiValueHeaders.isEmpty()) {
                return new LambdaResponse(statusCode, null, asJson(body));
            }

            return new LambdaResponse(statusCode, multiValueHeaders, asJson(body));
        }

    }

}
