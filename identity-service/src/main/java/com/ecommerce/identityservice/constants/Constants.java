package com.ecommerce.identityservice.constants;

import lombok.Getter;

@Getter
public enum Constants {
    SUCCESS(200, "Đăng nhập thành công"),
    UNAUTHORIZED(4001, "Tài khoản chưa xác thực"),
    TOKEN_FAIL(4002, "Mã xác thực không hợp lệ."),
    //server
    INTERNAL_SERVER(5000, "Không thể xử lí yêu cầu.Vui lòng thử lại sau."),
    //token
    TOKEN_VALIDATE(4002, "Phiên đăng nhập không hợp lệ. Vui lòng thử lại."),
    TOKEN_EXPIRED(4003, "Mã xác thực hết hạn."),
    USER_NOTFOUND(4004, "Không tìm thấy người dùng"),
    //register
    REGISTER_FAIL(4005, "Không thể đăng kí người dùng. Vui lòng thử lại sau"),
    EXISTS_USER(4006, "Email đã tồn tại trong hệ thống"),
    REGISTER_VALIDATE(4007, "Dữ liệu không hợp lệ"),
    REGISTER_SUCCESS(4008, "Đăng kí người dùng thành công"),

    //verify-email
    OTP_EXIST(4009, "OTP không tồn tại."),
    OTP_FAIL(4010, "OTP không hợp lệ"),
    OTP_EXPIRE(4011, "OTP đã hết hạn."),
    USER_ALREADY_VERIFIED(4012, "Tài khoản đã xác minh email"),
    RESEND_OTP_SUCCESS(4013, "Mã xác nhận đã được gửi tới email. Vui lòng kiểm tra email và điền mã xác nhận"),
    //profile
    PROFILE_NOT_FOUND(500, "Người dùng không tồn tại"),
    PROFILE_VALIDATE(500, "Dữ lệu không hợp lệ"),
    PROFILE_UPDATE_SUCCESS(200, "Cập nhật thng tin cá nhân thành công"),
    PROFILE_UPDATE_FAIL(500, "Không thể cập nhật thông tin. Vui lòng thử lại sau"),
    //role
    ROLE_VALIDATE(500, "Dữ liệu không hợp lệ. Vui lòng thử lại sau."),
    ROLE_EXISTS(500, "Quyền hạn đã tồn tại. Vui lòng thử lại."),
    //function
    FUNCTION_EXISTS(500, "Chức năng đã tồn tại. Vui lòng thử lại."),
    //login
    LOGIN_BLOCK(401, "Tài khoản của bạn đã bị khoá. Vui lòng liên hệ quản trị viên để được hỗ trợ"),
    LOGIN_EXPIRED(419, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"),
    LOGIN_VALIDATE(500, "Thông tin đăng nhập không hợp lệ"),
    LOGIN_NOT_FOUND(500, "Tài khoản chưa được đăng kí."),
    LOGIN_PASS_NOT_MATCH(500, "Email hoặc mật khẩu không đúng."),
    //logout
    LOGOUT_ERROR(500, "Không thể đăng xuất tài khoản. Vui lòng thử lại sau");








    private int code;
    private String message;

    Constants(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
