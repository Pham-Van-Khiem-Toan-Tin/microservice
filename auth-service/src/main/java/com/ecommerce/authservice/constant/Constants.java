package com.ecommerce.authservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constants {
    //Validate
    VALIDATE_FAIL(4001, "Dữ liệu không hợp lệ."),
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
    DELETE_SUBFUNCTION_SUCCESS(1005, "Xóa quyền hạn thành công");
    private final int code;
    private final String message;
}
