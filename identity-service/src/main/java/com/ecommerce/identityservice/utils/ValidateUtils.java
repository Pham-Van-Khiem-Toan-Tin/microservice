package com.ecommerce.identityservice.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidateUtils {

    public static boolean validateEmail(String email) {
        return StringUtils.hasText(email);
//                && Pattern.compile("^(.+)@(\\\\S+)$")
//                .matcher(email)
//                .matches();
    }

    public static boolean validatePassword(String password) {
        return StringUtils.hasText(password);
//                && Pattern.compile("/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,16}$/")
//                .matcher(password)
//                .matches();

    }
}
