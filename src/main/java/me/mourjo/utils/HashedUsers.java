package me.mourjo.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class HashedUsers {

    private static final String FILE_NAME = "verified_users.txt";
    private static final Set<String> verifiedUserHashes = HashedUsers.readVerifiedUserHashes();

    private static Set<String> readVerifiedUserHashes() {
        try (var inputStream = HashedUsers.class.getClassLoader().getResourceAsStream(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            return reader.lines().collect(Collectors.toSet());

        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return Set.of();
    }

    public static boolean isVerified(String email) {
        if (email != null) {
            var hash = HashGenerator.hash(email);
            return hash.isPresent() && verifiedUserHashes.contains(hash.get());
        }
        return false;
    }

}
