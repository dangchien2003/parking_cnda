package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.AddCartRequest;
import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.CountQuantityItemInCartResponse;
import com.parking.ticket_service.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/cart")
public class CartController {

    CartService cartService;

    @PostMapping
    ApiResponse<Void> add(@Valid @RequestBody AddCartRequest request) {
        cartService.addCart(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("count")
    ApiResponse<CountQuantityItemInCartResponse> count(@Valid @Min(value = 1, message = "INVALID_PAGE")
                                                       @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        return ApiResponse.<CountQuantityItemInCartResponse>builder()
                .result(cartService.count())
                .build();
    }

    @GetMapping("/all")
    ApiResponse<Object> getAll(@Valid @Min(value = 1, message = "INVALID_PAGE")
                               @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        return ApiResponse.<Object>builder()
                .result(cartService.getAll(page))
                .build();
    }
}
