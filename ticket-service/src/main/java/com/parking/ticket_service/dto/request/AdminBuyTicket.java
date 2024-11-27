package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminBuyTicket {
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String categoryId;
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String start;
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String end;

    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Size(min = 1, message = "Phải có ít nhất 1 tài khoản")
    List<String> emails;
}
