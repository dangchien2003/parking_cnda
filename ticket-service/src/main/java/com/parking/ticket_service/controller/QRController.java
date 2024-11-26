package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.CreateQrRequest;
import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.QrResponse;
import com.parking.ticket_service.service.QRService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/qr")
public class QRController {

    QRService qrService;

    @PostMapping("create")
    ApiResponse<QrResponse> create(@Valid @RequestBody CreateQrRequest request) {
        return ApiResponse.<QrResponse>builder()
                .result(qrService.add(request.getTicketId()))
                .build();
    }

    @GetMapping("get-all")
    ApiResponse<List<QrResponse>> getAll(@RequestParam("ticket") String ticketId) {
        return ApiResponse.<List<QrResponse>>builder()
                .result(qrService.getAllQrByTicket(ticketId))
                .build();
    }

    @GetMapping("get-new")
    ApiResponse<QrResponse> getNew(@RequestParam("ticket") String ticketId) {
        return ApiResponse.<QrResponse>builder()
                .result(qrService.getNew(ticketId))
                .build();
    }
}
