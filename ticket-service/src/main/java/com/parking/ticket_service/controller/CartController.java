package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.AddCartRequest;
import com.parking.ticket_service.dto.request.UpdateQuantityCartItemRequest;
import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.CountQuantityItemInCartResponse;
import com.parking.ticket_service.dto.response.ItemCartResponse;
import com.parking.ticket_service.service.CartService;
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
    ApiResponse<List<ItemCartResponse>> getAll(@Valid @Min(value = 1, message = "INVALID_PAGE")
                                               @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        return ApiResponse.<List<ItemCartResponse>>builder()
                .result(cartService.getAll(page))
                .build();
    }

    @PutMapping("/edit/quantity")
    ApiResponse<UpdateQuantityCartItemRequest> updateQuantity(@Valid @RequestBody UpdateQuantityCartItemRequest request) {
        return ApiResponse.<UpdateQuantityCartItemRequest>builder()
                .result(cartService.updateQuantity(request))
                .build();
    }

    @DeleteMapping("/{ticketId}")
    ApiResponse<Void> moveItem(@PathVariable(name = "ticketId") String ticket) {
        cartService.moveItem(ticket);
        return ApiResponse.<Void>builder()
                .build();
    }
}
