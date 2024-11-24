package com.parking.vault_service.controller;

import com.parking.vault_service.dto.request.DepositApproveRequest;
import com.parking.vault_service.dto.request.DepositCreationRequest;
import com.parking.vault_service.dto.request.StaffCancelDepositRequest;
import com.parking.vault_service.dto.response.*;
import com.parking.vault_service.entity.Deposit;
import com.parking.vault_service.entity.Fluctuation;
import com.parking.vault_service.service.ApproveDeposit;
import com.parking.vault_service.service.DepositService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/deposit")
public class DepositController {

    DepositService depositService;
    ApproveDeposit approveDeposit;

    @PostMapping
    ApiResponse<DepositResponse> create(HttpServletRequest http,
                                        @Valid @RequestBody DepositCreationRequest request) {
        return ApiResponse.<DepositResponse>builder()
                .result(depositService.create(http, request))
                .build();
    }

    @GetMapping("internal/callback/check")
    ApiResponse<Void> checkDeposit(HttpServletRequest http, @RequestParam("vnp_TxnRef") String depositId) {
        depositService.checkDeposit(http, depositId);
        return ApiResponse.<Void>builder()
                .build();
    }


    @GetMapping("/all/{type}")
    ApiResponse<PageResponse<Deposit>> getAll(
            @PathVariable(name = "type")
            String type,
            @RequestParam(name = "page")
            @Min(value = 1)
            int page,
            @RequestParam(name = "sort", required = false)
            String sort
    ) {
        return ApiResponse.<PageResponse<Deposit>>builder()
                .result(depositService.getAll(type, page, sort))
                .build();
    }

    @GetMapping("/{type}")
    ApiResponse<Object> getAccepted(
            @PathVariable(name = "type")
            String type,
            @RequestParam(name = "page")
            @Min(value = 1)
            int page,
            @RequestParam(name = "sort", required = false)
            String sort
    ) {
        return ApiResponse.<Object>builder()
                .result(depositService.customerGetAll(type, page, sort))
                .build();
    }

    @PutMapping("/cancel/{id}")
    ApiResponse<Void> cancel(@PathVariable(name = "id") String id) {
        depositService.cancelDeposit(id);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PutMapping("/cancel")
    ApiResponse<List<String>> cancel(@Valid @RequestBody StaffCancelDepositRequest request) {
        return ApiResponse.<List<String>>builder()
                .result(depositService.cancelDeposit(request))
                .build();
    }

    @GetMapping("/banked/all")
    ApiResponse<List<TransactionInfo>> deposit(@RequestParam(name = "date", required = true) String date) {
        return ApiResponse.<List<TransactionInfo>>builder()
                .result(depositService.getHistoryBanked(date))
                .build();
    }

    @GetMapping("/auto/approve")
    ApiResponse<Void> autoApprove() {
        approveDeposit.autoApprove();
        return ApiResponse.<Void>builder()
                .build();
    }


    @PostMapping("approve")
    ApiResponse<List<Fluctuation>> approve(@Valid @RequestBody DepositApproveRequest request) {
        return ApiResponse.<List<Fluctuation>>builder()
                .result(depositService.approveDeposit(request))
                .build();
    }

    @GetMapping("total")
    ApiResponse<Integer> totalDeposit() {
        return ApiResponse.<Integer>builder()
                .result(depositService.totalDeposit())
                .build();
    }

    @GetMapping("wait")
    ApiResponse<Integer> totalWaitApprove() {
        return ApiResponse.<Integer>builder()
                .result(depositService.totalWaitApprove())
                .build();
    }

    @GetMapping("approved")
    ApiResponse<Integer> totalApproved() {
        return ApiResponse.<Integer>builder()
                .result(depositService.totalApproved())
                .build();
    }

    @GetMapping("history")
    ApiResponse<List<HistoryDeposit>> history(@RequestParam(value = "date", required = false) String date,
                                              @RequestParam(value = "status", required = false) String status,
                                              @RequestParam(value = "page", defaultValue = "1") int page) {
        return ApiResponse.<List<HistoryDeposit>>builder()
                .result(depositService.history(page, status, date))
                .build();
    }
}
