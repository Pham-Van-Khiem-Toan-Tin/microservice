package com.ecommerce.authservice.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    // --- SỬA ĐỔI QUAN TRỌNG ---
                    // Thay vì sửa cookie cũ, ta tạo một Cookie MỚI để gửi lệnh xóa
                    Cookie deleteCookie = new Cookie(name, "");

                    // 1. Path bắt buộc phải giống hệt lúc tạo (chúng ta luôn set là "/")
                    deleteCookie.setPath("/");

                    // 2. Set thời gian sống = 0 để trình duyệt xóa ngay lập tức
                    deleteCookie.setMaxAge(0);

                    // 3. Các thông số khác cũng nên giống lúc tạo
                    deleteCookie.setHttpOnly(true);
                    deleteCookie.setSecure(true); // Nếu bạn chạy https, còn localhost thì true/false cũng được

                    // 4. Gửi về trình duyệt
                    response.addCookie(deleteCookie);
                }
            }
        }
    }

    // --- SỬA ĐOẠN NÀY (Thay thế SerializationUtils của Spring) ---

    // 1. Serialize: Object -> Chuỗi Base64
    public static String serialize(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            return Base64.getUrlEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace(); // Nên dùng Logger trong thực tế
            return null;
        }
    }

    // 2. Deserialize: Chuỗi Base64 -> Object
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            // 1. Kiểm tra cơ bản
            if (cookie.getValue() == null || cookie.getValue().isEmpty()) {
                return null;
            }

            // 2. Giải mã Base64
            byte[] data = Base64.getUrlDecoder().decode(cookie.getValue());
            if (data == null || data.length == 0) {
                return null;
            }

            // 3. Đọc Object (Nơi hay xảy ra lỗi EOF)
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

                return cls.cast(objectInputStream.readObject());
            }
        } catch (Exception e) {
            // Bắt mọi lỗi: EOFException, StreamCorruptedException, ClassNotFoundException...
            // Chỉ log nhẹ để debug, không throw exception ra ngoài
            System.err.println("Cookie deserialize failed (Will ignore): " + e.toString());
            return null;
        }
    }
}
