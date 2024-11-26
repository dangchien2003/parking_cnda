package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.UpdateSettingRequest;
import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.entity.Setting;
import com.parking.ticket_service.service.SettingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/setting")
public class SettingController {
    SettingService settingService;

    @PutMapping
    ApiResponse<Void> update(@Valid @RequestBody UpdateSettingRequest request) {
        settingService.update(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    ApiResponse<Setting> get() {
        return ApiResponse.<Setting>builder()
                .result(settingService.getNewRecord())
                .build();
    }
}
