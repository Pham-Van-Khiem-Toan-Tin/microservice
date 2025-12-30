package com.ecommerce.catalogservice.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^a-zA-Z0-9-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private SlugUtils() {}
    public static String toSlug(String input) {
        if (input == null) return null;

        // 1️⃣ xử lý riêng cho đ / Đ
        String processed = input
                .replace("đ", "d")
                .replace("Đ", "D");

        // 2️⃣ thay whitespace
        String nowhitespace = WHITESPACE.matcher(processed).replaceAll("-");

        // 3️⃣ normalize
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);

        // 4️⃣ remove non latin
        String slug = NONLATIN.matcher(normalized).replaceAll("");

        return slug
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

}
