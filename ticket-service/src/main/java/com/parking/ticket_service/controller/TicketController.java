package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.AdminBuyTicket;
import com.parking.ticket_service.dto.request.BuyTicketRequest;
import com.parking.ticket_service.dto.request.TicketUpdatePlateRequest;
import com.parking.ticket_service.dto.response.*;
import com.parking.ticket_service.entity.Ticket;
import com.parking.ticket_service.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketController {

    TicketService ticketService;

    @PostMapping("/buy")
    ApiResponse<TicketResponse> buy(@Valid @RequestBody BuyTicketRequest request) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.buy(request))
                .build();
    }

    @GetMapping("/{ticket}")
    ApiResponse<TicketResponse> info(@PathVariable(name = "ticket") String ticket) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.getInfoTicket(ticket))
                .build();
    }

    @GetMapping("admin/{ticket}")
    ApiResponse<TicketResponse> infoGetInfo(@PathVariable(name = "ticket") String ticket) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.getInfoTicketADMIN(ticket))
                .build();
    }


    @PutMapping("/plate")
    ApiResponse<TicketResponse> updatePlate(@Valid @RequestBody TicketUpdatePlateRequest request) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.updatePlate(request))
                .build();
    }

    @PutMapping("/cancel/{ticketId}")
    ApiResponse<Void> cancelTicket(@PathVariable(name = "ticketId", required = true) String ticket) {
//        ticketService.cancel(ticket);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PatchMapping("/extend/{ticketId}")
    ApiResponse<Void> extendTicket(@PathVariable(name = "ticketId") String ticket,
                                   @RequestParam(name = "date") String date) {
        ticketService.extendTicket(ticket, date);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/qr/{ticketId}")
    ApiResponse<String> renderQr(@PathVariable(name = "ticketId") String ticket) throws Exception {
        return ApiResponse.<String>builder()
                .result(ticketService.renderQr(ticket))
                .build();
    }

    //    @GetMapping("mn/da-ban")
//    ApiResponse<List<Object>> layVeDaBan(@RequestParam(name = "page", defaultValue = "1", required = false) int page,
//                                         @RequestParam(name = "start", required = false) String start,
//                                         @RequestParam(name = "end", required = false) String end,
//                                         @RequestParam(name = "email", required = false) String email,
//                                         @RequestParam(name = "plate", required = false) String plate,
//                                         @RequestParam(name = "vehicle", required = false) String vehicle
//    ) {
//
//    }
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    @GetMapping("thong-ke/tk-ve-ban")
    ApiResponse<List<tkvbResponse>> tkVeBan(@RequestParam("date") String date) {
        return ApiResponse.<List<tkvbResponse>>builder()
                .result(ticketService.tkVeBan(date))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    @GetMapping("thong-ke/tk-doanh-thu")
    ApiResponse<List<tkdtResponse>> tkdt(@RequestParam("date") String date) {
        return ApiResponse.<List<tkdtResponse>>builder()
                .result(ticketService.tkdt(date))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    @GetMapping("ds-ve-ban")
    ApiResponse<List<Ticket>> dsVeBan(@RequestParam("date") String date) {
        return ApiResponse.<List<Ticket>>builder()
                .result(ticketService.getListBetween(date))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    @GetMapping("timkiem-ds-ve-ban")
    ApiResponse<List<Ticket>> tkdsVeBan(@RequestParam("start") String start,
                                        @RequestParam("end") String end,
                                        @RequestParam(name = "vehicle", required = false) String vehicle,
                                        @Valid
                                        @Min(value = 1, message = "Số trang phải lớn hơn 1")
                                        @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        return ApiResponse.<List<Ticket>>builder()
                .result(ticketService.tkdsVeBan(start, end, vehicle, page))
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    @PostMapping("buy/adminBuyForListEmail")
    ApiResponse<List<Ticket>> adminBuyForListEmail(@Valid @RequestBody AdminBuyTicket request) {
        return ApiResponse.<List<Ticket>>builder()
                .result(ticketService.adminBuyForListEmail(request))
                .build();
    }


//    @PostMapping("/checkin/first")
//    ApiResponse<Void> firstCheckin(@RequestHeader(name = "station") String station,
//                                   @Valid @RequestBody FirstCheckinRequest request) throws JsonProcessingException {
//        ticketService.checkinFirstStep(station, request);
//        return ApiResponse.<Void>builder()
//                .build();
//    }
//
//    @PostMapping("/checkin/second")
//    ApiResponse<Void> secondCheckin(@RequestHeader(name = "station") String station,
//                                    @Valid @RequestBody SecondCheckinRequest request) throws JsonProcessingException {
//        ticketService.checkinSecondStep(station, request);
//        return ApiResponse.<Void>builder()
//                .build();
//    }
//
//    @PostMapping("/checkout/first")
//    ApiResponse<Void> firstCheckout(@RequestHeader(name = "station") String station,
//                                    @Valid @RequestBody FirstCheckoutRequest request) throws JsonProcessingException {
//        ticketService.checkoutFirstStep(station, request);
//        return ApiResponse.<Void>builder()
//                .build();
//    }
//
//    @PostMapping("/checkout/second")
//    ApiResponse<Void> secondCheckout(@RequestHeader(name = "station") String station,
//                                     @Valid @RequestBody SecondCheckoutRequest request) throws JsonProcessingException {
//        ticketService.checkoutSecondStep(station, request);
//        return ApiResponse.<Void>builder()
//                .build();
//    }

    @GetMapping("/all")
    ApiResponse<List<TicketResponse>> getAll(@RequestParam("page") int page,
                                             @RequestParam("vehicle") String vehicle) {
        return ApiResponse.<List<TicketResponse>>builder()
                .result(ticketService.getAll(page, vehicle))
                .build();
    }

    @GetMapping("count/purchased")
    ApiResponse<Integer> countTicketPurchased() {
        return ApiResponse.<Integer>builder()
                .result(ticketService.countTicketPurchased())
                .build();
    }


    @GetMapping("count/use-times-in-month")
    ApiResponse<Integer> countUseTimesInMonth() {
        return ApiResponse.<Integer>builder()
                .result(ticketService.countUseTimesInMonth())
                .build();
    }

    @GetMapping("count/total-turn")
    ApiResponse<Integer> getTotalTurn() {
        return ApiResponse.<Integer>builder()
                .result(ticketService.getTotalTurn())
                .build();
    }

    @GetMapping("recent_activity")
    ApiResponse<List<RecentActivityResponse>> getRecentActivity() {
        return ApiResponse.<List<RecentActivityResponse>>builder()
                .result(ticketService.getRecentActivity())
                .build();
    }

}
