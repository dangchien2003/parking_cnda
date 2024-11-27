package com.parking.profile_service.controller;

import com.parking.profile_service.dto.request.CustomerProfileCreationRequest;
import com.parking.profile_service.dto.response.ApiResponse;
import com.parking.profile_service.dto.response.CustomerProfileResponse;
import com.parking.profile_service.entity.ProfileCustomer;
import com.parking.profile_service.service.CustomerProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/internal/customer")
public class InternalCustomerProfileController {

    CustomerProfileService customerProfileService;

    @PostMapping
    ApiResponse<CustomerProfileResponse> createProfile(@RequestBody CustomerProfileCreationRequest request) {
        return ApiResponse.<CustomerProfileResponse>builder()
                .result(customerProfileService.createProfile(request))
                .build();
    }

    @GetMapping("get-by-name")
    ApiResponse<List<ProfileCustomer>> getAllByName(@RequestParam("name") String name,
                                                    @RequestParam("page") int page) {
        return ApiResponse.<List<ProfileCustomer>>builder()
                .result(customerProfileService.getCustomerByName(name, page))
                .build();
    }

    @GetMapping("get-by-list-id")
    ApiResponse<List<ProfileCustomer>> getAllByName(@RequestParam("ids") List<String> ids) {
        return ApiResponse.<List<ProfileCustomer>>builder()
                .result(customerProfileService.getCustomerByIds(ids))
                .build();
    }
}
