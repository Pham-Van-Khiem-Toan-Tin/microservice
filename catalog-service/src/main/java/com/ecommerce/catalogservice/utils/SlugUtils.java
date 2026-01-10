package com.ecommerce.catalogservice.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern DASHES = Pattern.compile("-{2,}");

    private SlugUtils() {}
    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }

        // trim + lowercase
        String result = input.trim().toLowerCase(Locale.ROOT);

        // normalize unicode (remove accents)
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // đ/Đ special case
        result = result.replace("đ", "d");

        // whitespace -> dash
        result = WHITESPACE.matcher(result).replaceAll("-");

        // remove non latin chars
        result = NONLATIN.matcher(result).replaceAll("");

        // collapse multiple dashes
        result = DASHES.matcher(result).replaceAll("-");

        // trim dash
        result = result.replaceAll("^-|-$", "");

        return result;
    }

}
