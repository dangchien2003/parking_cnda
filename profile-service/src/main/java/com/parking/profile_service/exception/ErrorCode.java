package com.parking.profile_service.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public enum ErrorCode {
    INVALID_PASSWORD(4001, "mật khẩu ít hơn {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_EXISTED(4002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXIST(4003, "Hồ sơ không tồn tại", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(4004, "Mật khẩu không đúng", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAIL(4005, "Tên người dùng hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_BLOCKED(4006, "Tài khoản đã bị chặn", HttpStatus.FORBIDDEN),
    INVALID_EMAIL(4007, "Định dạng email không đúng", HttpStatus.BAD_REQUEST),
    INVALID_KEY(4008, "Tin nhắn không hợp lệ key", HttpStatus.BAD_REQUEST),
    BLANK_TOKEN(4009, "Không thể để trống mã thông báo", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(4010, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    NOTFOUND_METHOD(4011, "Url không hỗ trợ phương thức", HttpStatus.BAD_REQUEST),
    BODY_PARSE_FAIL(4012, "Phân tích cú pháp nội dung không thành công", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(4013, "Mã thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
    FIELD_INFORMATION_MISSING(4014, "Thông tin trường bị thiếu: {field}", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE(4015, "Không thể xóa đối tượng", HttpStatus.BAD_REQUEST),
    NOTFOUND_URL(4016, "Url không tồn tại", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(4017, "Không có quyền", HttpStatus.FORBIDDEN),
    INVALID_BIRTHDAY(4018, "Tuổi của bạn phải ít nhất là {min}", HttpStatus.BAD_REQUEST),
    INVALID_LIST_UID(4019, "Danh sách uid phải có ít nhất {min} phần tử", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_EXISTED(4020, "Số điện thoại đã được tạo", HttpStatus.BAD_REQUEST),
    INVALID_DATA(4021, "Dữ liệu không hợp lệ, hãy kiểm tra lại", HttpStatus.BAD_REQUEST),

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
