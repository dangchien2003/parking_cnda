package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.PlateResponse;
import com.parking.ticket_service.service.PlateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/plate")
public class PlateController {

    PlateService plateService;


    @GetMapping("all")
    ApiResponse<List<PlateResponse>> getPlate(@RequestParam("ticket") String ticketId) {
        return ApiResponse.<List<PlateResponse>>builder()
                .result(plateService.getAllPlate(ticketId))
                .build();
    }
}
