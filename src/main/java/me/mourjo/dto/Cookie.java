package me.mourjo.dto;

import lombok.Builder;

@Builder
public record Cookie(String name, String value, long maxAge, SameSite sameSite) {

    @Override
    public String toString() {
        return """
            %s=%s; Max-Age=%s; SameSite=%s; Secure; HttpOnly""".formatted(name, value, maxAge,
            sameSite.toString());
    }

    public enum SameSite {
        STRICT, LAX, NONE;

        @Override
        public String toString() {
            return switch (this) {
                case STRICT -> "Strict";
                case LAX -> "Lax";
                default -> "None";
            };
        }
    }
}
