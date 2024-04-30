package me.mourjo.utils;

import java.util.Optional;

public class CookieUtils {

    public static Optional<String> findCookieByName(String cookieHeader, String cookieName) {
        for (String cookie : cookieHeader.split("; ")) {
            String[] nameValuePair = cookie.split("=");
            if (nameValuePair.length == 2) {
                String name = nameValuePair[0];
                String value = nameValuePair[1];
                if (cookieName.equals(name)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }
}
