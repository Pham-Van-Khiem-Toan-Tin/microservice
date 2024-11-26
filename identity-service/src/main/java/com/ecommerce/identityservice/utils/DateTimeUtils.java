package com.ecommerce.identityservice.utils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static String convertToTimeBetweenString(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(endTime, startTime);
        long minutes = Math.abs(duration.toMinutes());
        long seconds = Math.abs(duration.toSeconds() % 60);
        return minutes + " phút " + seconds + " giây.";
    }
    public static String convertToTimeString(LocalDateTime time, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }
    public static LocalDateTime convertTimeStampToLocalDateTime(Timestamp timestamp) {
        LocalDateTime result = null;
        if (timestamp != null)
            result = timestamp.toLocalDateTime();
        return result;
    }
}
