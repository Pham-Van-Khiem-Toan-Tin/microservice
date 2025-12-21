package com.ecommerce.identityservice.utils;

import java.security.SecureRandom;

public class OtpUtils {
    private static final SecureRandom random = new SecureRandom();

    public static String generate6Digit() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
