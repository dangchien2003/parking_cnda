package com.parking.ticket_service.exception;

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
    INVALID_KEY(6008, "Sai dữ liệu", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(6010, "Cần xác thực tài khoản", HttpStatus.UNAUTHORIZED),
    NOTFOUND_METHOD(6011, "Không tìm thấy đường dẫn", HttpStatus.BAD_REQUEST),
    BODY_PARSE_FAIL(6012, "Không thể giải mã dữ liệu", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(6013, "Định danh không có hiệu lực", HttpStatus.BAD_REQUEST),
    FIELD_INFORMATION_MISSING(6014, "Không được bỏ trống trường: {field}", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE(6015, "Không thể xoá", HttpStatus.BAD_REQUEST),
    NOTFOUND_URL(6016, "Không tìm thấy đường dẫn", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(6017, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_DATA(6021, "Dữ liệu không đúng", HttpStatus.BAD_REQUEST),
    ADDRESS_EXISTED(6022, "Trạm đã tồn tại", HttpStatus.BAD_REQUEST),
    NOTFOUND_FILTER(6023, "Không tìm thấy bộ lọc", HttpStatus.BAD_REQUEST),
    STATION_NOT_EXIST(6024, "Không tìm thấy trạm", HttpStatus.BAD_REQUEST),
    UPDATE_FAIL(6025, "Cập nhật thất bại", HttpStatus.BAD_REQUEST),
    DATA_EXISTED(6026, "Dữ liệu đã tồn tại", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND(6027, "Dữ liệu không tồn tại", HttpStatus.BAD_REQUEST),
    STATION_NOT_FOUND(6028, "Không tìm thấy trạm", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(6029, "Sai địa chỉ", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY(6030, "Sai thông tin vé", HttpStatus.BAD_REQUEST),
    CANNOT_BUY_TICKET(6030, "Không thể mua vé", HttpStatus.BAD_REQUEST),
    NOTFOUND_CATEGORY_UNIT(6031, "Không tìm thấy vé", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(6032, "Không đủ số dư", HttpStatus.BAD_REQUEST),
    PROCESS_FAIL(6033, "Lỗi xử lý", HttpStatus.BAD_REQUEST),
    TICKET_NOTFOUND(6034, "Vé không tồn tại", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_PLATE(6035, "Cập nhật biển số thất bại", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_TICKET(6036, "Cập nhật thất bại", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_DATETIME(6037, "Lỗi định dạng thời gian", HttpStatus.BAD_REQUEST),
    INVALID_NEW_EXPIRE(6038, "Thời gian sử dụng còn ít nhất là 15 phút", HttpStatus.BAD_REQUEST),
    EXTEND_FAIL(6039, "Vé không đủ điều kiện gia hạn", HttpStatus.BAD_REQUEST),
    TICKET_NO_LONGER_VALID(6040, "Vé không còn hiệu lực", HttpStatus.BAD_REQUEST),
    STATION_NOT_SUPPORT(6041, "Trạm không còn hoạt động", HttpStatus.BAD_REQUEST),
    INVALID_PLATE(6042, "Số biển số không đúng", HttpStatus.BAD_REQUEST),
    TICKET_IN_USE(6043, "Vé đang sử dụng", HttpStatus.BAD_REQUEST),
    ERROR_TICKET(6044, "Vé lỗi", HttpStatus.BAD_REQUEST),
    STATION_NOT_SUPPORT_TICKET(6045, "Trạm không hỗ trợ vé này", HttpStatus.BAD_REQUEST),
    UNUSED_TICKET(6046, "Vé chưa sử dụng", HttpStatus.BAD_REQUEST),
    DIFFERENT_STATION(6047, "Không đúng trạm", HttpStatus.BAD_REQUEST),
    CHECKIN_NOT_YET(6048, "Vé chưa được kiểm tra", HttpStatus.BAD_REQUEST),
    INCORRECT_PLATE(6049, "Biển số xe không đúng", HttpStatus.BAD_REQUEST),
    INVALID_CODE(6050, "Mã không hợp lệ", HttpStatus.BAD_REQUEST),
    TIME_USE_TOO_CLOSE(6051, "Thao tác quá nhanh", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_VALUE(6052, "Giá trị giảm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_MAX(6053, "Giá trị giảm tối đa không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_MIN(6054, "Giá trị giảm tối thiểu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_COUPON_TIME(6055, "Thời gian phiếu giảm giá không hợp lệ", HttpStatus.BAD_REQUEST),
    COUPON_APPROVED(6056, "Phiếu giảm giá đã được chấp thuận", HttpStatus.BAD_REQUEST),
    INVALID_PAGE(6057, "Số trang không hợp lệ", HttpStatus.BAD_REQUEST),
    QUANTITY_TOO_SMALL(6058, "Số lượng phải lớn hơn 1", HttpStatus.BAD_REQUEST),

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
