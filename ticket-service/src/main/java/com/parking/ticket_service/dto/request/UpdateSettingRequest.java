package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSettingRequest {
    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Min(value = 1, message = "Số vị trí đỗ cho ô tô tối thiều là 1")
    @Max(value = 100000, message = "Số vị trí đỗ cho ô tô tối đa là 100000")
    Integer maxPositionCar;
    
    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Min(value = 1, message = "Số vị trí dự trữ cho ô tô tối thiều là 1")
    @Max(value = 1000, message = "Số vị trí dự trữ cho ô tô tối đa là 1000")
    int spareCar;

    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Min(value = 1, message = "Số vị trí đỗ cho xe máy tối thiều là 1")
    @Max(value = 100000, message = "Số vị trí đỗ cho xe máy tối đa là 100000")
    int maxPositionMotorbike;

    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Min(value = 1, message = "Số vị trí dự trữ cho ô tô tối thiều là 1")
    @Max(value = 1000, message = "Số vị trí dự trữ cho ô tô tối đa là 1000")
    int spareMotorbike;
}
