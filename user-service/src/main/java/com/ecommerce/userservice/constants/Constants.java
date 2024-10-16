package com.ecommerce.userservice.constants;

public class Constants {
    public enum Auth {
        SUCCESS(200, "Đăng nhập thành công"),
        UNAUTHORIZED(401, "Tài khoản chưa xác thực"),
        TOKEN_FAIL(410, "Token exprise");
        private final int code;
        private final String message;

        Auth(int code, String message) {
            this.code = code;
            this.message = message;
        }

    }
}
