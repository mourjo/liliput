package me.mourjo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class HashGenerator {

    public static Optional<String> hash(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        try {
            // Get an instance of SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Compute the hash
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return Optional.of(hexString.toString());

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithm not found: " + e.getMessage());
        }
        return Optional.empty();
    }

}
