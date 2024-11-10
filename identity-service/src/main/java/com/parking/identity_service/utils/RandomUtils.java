package com.parking.identity_service.utils;

import java.util.Random;

public class RandomUtils {
    private RandomUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    static Random random = new Random();

    public static String randomPassword() {
        int length = 10;
        int start = 32;
        int end = 126;
        return runner(length, start, end);
    }

    public static String runner(int length, int start, int end) {
        StringBuilder randomString = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomAscii = start + random.nextInt(end - start + 1);
            randomString.append((char) randomAscii);
        }

        return randomString.toString();
    }
}
