package com.ecommerce.identityservice.constants;

import lombok.Getter;

@Getter
public enum Constants {
    SUCCESS(200, "Đăng nhập thành công"),
    UNAUTHORIZED(401, "Tài khoản chưa xác thực"),
    TOKEN_FAIL(410, "Token exprise"),
    //server
    INTERNAL_SERVER(500, "Không thể xử lí yêu cầu.Vui lòng thử lại sau."),
    //login
    LOGIN_VALIDATE(500, "Thông tin đăng nhập không hợp lệ"),
    LOGIN_NOT_FOUND(500, "Tài khoản chưa được đăng kí."),
    LOGIN_PASS_NOT_MATCH(500, "Email hoặc mật khẩu không đúng."),
    //register
    REGISTER_FAIL(500, "Không thể đăng kí người dùng. Vui lòng thử lại sau"),
    EXISTS_USER(500, "Email đã tồn tại trong hệ thống"),
    REGISTER_VALIDATE(500, "Dữ liệu không hợp lệ"),
    REGISTER_SUCCESS(200, "Đăng kí người dùng thành công");
    private int code;
    private String message;

    Constants(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
