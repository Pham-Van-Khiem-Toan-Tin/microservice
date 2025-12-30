package com.ecommerce.identityservice.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),
    //ROLE
    DELETE_ROLE_SUCCESS(1010, "Xóa vai trò thành công"),
    UPDATE_ROLE_SUCCESS(1011, "Cập nhật vai trò thành công"),
    CREATE_ROLE_SUCCESS(1010, "Thêm mới vai trò thành công"),
    ROLE_NOT_EXIST(1006, "Vai trò không tồn tại"),
    ROlE_EXIST(1006, "Vai trò đã tồn tại"),
    // FUNCTION
    DELETE_FUNCTION_SUCCESS(1008, "Xóa chức năng thành công"),
    UPDATE_FUNCTION_SUCCESS(1007, "Cập nhật chức năng thành công"),
    FUNCTION_NOT_EXIST(1006, "Chức năng không tồn tại"),
    FUNCTION_EXIST(1006, "Chức năng đã tồn tại"),
    CREAT_FUNCTION_SUCCESS(1005, "Thêm mới chức năng thành công."),
    // SUBFUNCTION
    SUBFUNCTION_EXIST(1004, "Quyền hạn đã tồn tại."),
    SUBFUNCTION_NOT_EXIST(1003, "Không tồn tại quyền hạn."),
    UPDATE_SUBFUNCTION_FAIL(1002, "Không thể cập nhật quyền hạn vui lòng thử lại sau."),
    UPDATE_SUBFUNCTION_SUCCESS(1005, "Cập nhật quyền hạn thành công."),
    CREATE_SUBFUNCTION_FAIL(1001, "Thêm mới quyền hạn không thành công vui lòng thử lại sau."),
    CREATE_SUBFUNCTION_SUCCESS(1000, "Thêm mới quyền hạn thành công."),
    DELETE_SUBFUNCTION_SUCCESS(1005, "Xóa quyền hạn thành công"),
    //login
    EMAIL_VERIFY(1015, "Vui lòng nhập mã xác nhận gửi đến email." ),
    LOGIN_BLOCK(401, "Tài khoản của bạn đã bị khoá. Vui lòng liên hệ quản trị viên để được hỗ trợ"),
    LOGIN_EXPIRED(419, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"),
    LOGIN_VALIDATE(500, "Thông tin đăng nhập không hợp lệ"),
    LOGIN_NOT_FOUND(500, "Tài khoản chưa được đăng kí."),
    LOGIN_PASS_NOT_MATCH(500, "Email hoặc mật khẩu không đúng."),
    //logout
    LOGOUT_ERROR(500, "Không thể đăng xuất tài khoản. Vui lòng thử lại sau");



    private final int code;
    private final String message;


}
