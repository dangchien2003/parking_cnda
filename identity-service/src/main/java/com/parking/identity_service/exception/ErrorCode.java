package com.parking.identity_service.exception;

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
    INVALID_PASSWORD(2001, "mật khẩu ít hơn {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_EXISTED(2002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(2003, "Người dùng không tồn tại", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(2004, "Mật khẩu không đúng", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAIL(2005, "Tên người dùng hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_BLOCKED(2006, "Tài khoản đã bị chặn", HttpStatus.FORBIDDEN),
    INVALID_EMAIL(2007, "Định dạng email không đúng", HttpStatus.BAD_REQUEST),
    INVALID_KEY(2008, "Tin nhắn không hợp lệ key", HttpStatus.BAD_REQUEST),
    BLANK_TOKEN(2009, "Không thể để trống mã thông báo", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(2010, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    NOTFOUND_METHOD(2011, "Url không hỗ trợ phương thức", HttpStatus.BAD_REQUEST),
    BODY_PARSE_FAIL(2012, "Phân tích cú pháp nội dung không thành công", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(2013, "Mã thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
    FIELD_INFORMATION_MISSING(2014, "Thiếu thông tin trường: {field}", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE(2015, "Không thể xóa đối tượng", HttpStatus.BAD_REQUEST),
    NOTFOUND_URL(2016, "Url không tồn tại", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(2017, "Không có quyền", HttpStatus.FORBIDDEN),
    INVALID_BIRTHDAY(2018, "Tuổi của bạn phải ít nhất là {min}", HttpStatus.BAD_REQUEST),
    INVALID_LIST_UID(2019, "Danh sách uid phải có ít nhất {min} phần tử", HttpStatus.BAD_REQUEST),

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi chưa phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
