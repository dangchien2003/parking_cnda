package com.parking.vault_service.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ErrorCode {
    OWNER_NOT_EXIST(5003, "Chủ sở hữu chưa tạo", HttpStatus.BAD_REQUEST),
    INVALID_KEY(5008, "Khóa tin nhắn không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(5010, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    NOTFOUND_METHOD(5011, "Url không hỗ trợ phương thức", HttpStatus.BAD_REQUEST),
    BODY_PARSE_FAIL(5012, "Phân tích cú pháp nội dung không thành công", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(5013, "Mã thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
    FIELD_INFORMATION_MISSING(5014, "Thông tin trường bị thiếu: {field}", HttpStatus.BAD_REQUEST),
    NOTFOUND_URL(5016, "Url không tồn tại", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(5017, "Không có quyền", HttpStatus.FORBIDDEN),
    TOO_SMALL_AMOUNT(5020, "Số tiền gửi quá nhỏ", HttpStatus.BAD_REQUEST),
    PARAM_MISSING(5021, "Thiếu tham số", HttpStatus.BAD_REQUEST),
    INCORRECT_PARAM_FORMAT(5022, "Tham số không đúng định dạng", HttpStatus.BAD_REQUEST),
    INCORRECT_DATA(5023, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILTERS(5024, "Bộ lọc không được hỗ trợ", HttpStatus.BAD_REQUEST),
    TYPE_NOT_EXIST(5025, "Không hiểu hành động cần thực hiện", HttpStatus.BAD_REQUEST),
    UPDATE_FAIL(5026, "Cập nhật không thành công", HttpStatus.BAD_REQUEST),
    LENGTH_TOO_SHORT(5027, "Dữ liệu phải có ít nhất: {min}", HttpStatus.BAD_REQUEST),
    WALLET_IN_STATUS(5028, "Ví đang ở trạng thái", HttpStatus.BAD_REQUEST),
    DEPOSIT_NOT_EXIST_OR_BEEN_APPROVED(5029, "Lệnh không tồn tại hoặc đã được chấp thuận", HttpStatus.BAD_REQUEST),
    NOT_FOUND_WALLET(5030, "Ví không tồn tại", HttpStatus.BAD_REQUEST),
    CANNOT_USE_WALLET(5031, "Không thể sử dụng ví", HttpStatus.BAD_REQUEST),
    DEPOSIT_FAIL(5032, "Gửi tiền không thành công", HttpStatus.BAD_REQUEST),
    INVALID_DATA(5033, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MANY_DEPOSIT(5034, "Bạn đã gửi lệnh quá 3 lần. Hãy huỷ giao dịch cũ", HttpStatus.BAD_REQUEST),

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
