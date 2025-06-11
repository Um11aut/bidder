package com.optimax.tradingbot.utils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomStringGenerator {

    RandomStringGenerator() {
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * generate pseudo-random string
     */
    public static String generateRandomString(int length) {
        Random random = new SecureRandom(); // Or new Random()
        return random.ints(length, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}