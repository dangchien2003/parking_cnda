package com.parking.vault_service.controller;

import com.parking.vault_service.dto.response.ApiResponse;
import com.parking.vault_service.dto.response.Fluctuation30DaysResponse;
import com.parking.vault_service.dto.response.FluctuationResponse;
import com.parking.vault_service.dto.response.PageResponse;
import com.parking.vault_service.entity.Fluctuation;
import com.parking.vault_service.service.FluctuationService;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/fluctuation")
public class FluctuationController {
    FluctuationService fluctuationService;

    @GetMapping("/all/{type}")
    ApiResponse<PageResponse<Fluctuation>> getAll(
            @PathVariable(name = "type")
            String type,

            @RequestParam(name = "page", defaultValue = "1")
            @Min(value = 1)
            int page,

            @RequestParam(name = "sort", required = false, defaultValue = "desc")
            String sort,

            @RequestParam(name = "field", required = false, defaultValue = "createAt")
            String field
    ) {
        return ApiResponse.<PageResponse<Fluctuation>>builder()
                .result(fluctuationService.getAllByStaff(type, page, sort, field))
                .build();
    }

    @GetMapping("/all/{type}/{uid}")
    ApiResponse<PageResponse<Fluctuation>> getAll(
            @PathVariable(name = "type")
            String type,

            @PathVariable(name = "uid")
            String uid,

            @RequestParam(name = "page", defaultValue = "1")
            @Min(value = 1)
            int page,

            @RequestParam(name = "sort", required = false, defaultValue = "desc")
            String sort,

            @RequestParam(name = "field", required = false, defaultValue = "createAt")
            String field
    ) {
        return ApiResponse.<PageResponse<Fluctuation>>builder()
                .result(fluctuationService.getAllByStaff(type, uid, page, sort, field))
                .build();
    }

    @GetMapping
    ApiResponse<List<FluctuationResponse>> customerGetAll(
            @RequestParam(name = "type", required = false)
            String type,

            @RequestParam(name = "page", defaultValue = "1")
            @Min(value = 1)
            int page,

            @RequestParam(name = "sort", required = false, defaultValue = "desc")
            String sort,

            @RequestParam(name = "field", required = false, defaultValue = "createAt")
            String field,

            @RequestParam(name = "date", required = false) String date
    ) {
        return ApiResponse.<List<FluctuationResponse>>builder()
                .result(fluctuationService.getAllByCustomer(type, date, page, sort, field))
                .build();
    }

    @GetMapping("/use-in-month")
    ApiResponse<Integer> getDebitInMonth(@RequestParam(value = "uid", required = false) String uid) {
        return ApiResponse.<Integer>builder()
                .result(fluctuationService.getUseInMonth(uid))
                .build();
    }

    @GetMapping("/fluctuation-in-30-day")
    ApiResponse<List<Fluctuation30DaysResponse>> getTimesUseIn30Days() {
        return ApiResponse.<List<Fluctuation30DaysResponse>>builder()
                .result(fluctuationService.fluctuationIn30Days())
                .build();
    }


//
//    nạp tiền thành công của user1 sắp xếp theo giảm dần của ngày tao, trang 1
//    nạp thu hồi tiền của user1 sắp xếp theo giảm dần của ngày tao, trang 1
//    mua vé của user1 sắp xếp theo giảm dần của ngày tao, trang 1
//    vé tăng hạn của user1 sắp xếp theo giảm dần của ngày tao, trang 1
//    phaan loại khác
}
