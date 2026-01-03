package com.ecommerce.catalogservice.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static String instantToString(Instant instant, String format) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(format);

        return instant
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))   // hoặc ZoneId.of("Asia/Ho_Chi_Minh")
                .format(formatter);
    }
}
