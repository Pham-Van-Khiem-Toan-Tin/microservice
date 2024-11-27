package com.ecommerce.identityservice.constants;

import lombok.Getter;

@Getter
public enum Constants {
    SUCCESS(200, "Đăng nhập thành công"),
    UNAUTHORIZED(401, "Tài khoản chưa xác thực"),
    TOKEN_FAIL(410, "Mã xác thực không hợp lệ."),
    //server
    INTERNAL_SERVER(500, "Không thể xử lí yêu cầu.Vui lòng thử lại sau."),
    //token
    TOKEN_VALIDATE(401, "Phiên đăng nhập không hợp lệ. Vui lòng thử lại."),
    TOKEN_EXPIRED(498, "Mã xác thực hết hạn."),
    //login
    LOGIN_BLOCK(401, "Tài khoản của bạn đã bị khoá. Vui lòng liên hệ quản trị viên để được hỗ trợ"),
    LOGIN_EXPIRED(419, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"),
    LOGIN_VALIDATE(500, "Thông tin đăng nhập không hợp lệ"),
    LOGIN_NOT_FOUND(500, "Tài khoản chưa được đăng kí."),
    LOGIN_PASS_NOT_MATCH(500, "Email hoặc mật khẩu không đúng."),
    //logout
    LOGOUT_ERROR(500, "Không thể đăng xuất tài khoản. Vui lòng thử lại sau"),
    //register
    REGISTER_FAIL(500, "Không thể đăng kí người dùng. Vui lòng thử lại sau"),
    EXISTS_USER(500, "Email đã tồn tại trong hệ thống"),
    REGISTER_VALIDATE(500, "Dữ liệu không hợp lệ"),
    REGISTER_SUCCESS(200, "Đăng kí người dùng thành công"),
    //profile
    PROFILE_NOT_FOUND(500, "Người dùng không tồn tại");
    private int code;
    private String message;

    Constants(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
