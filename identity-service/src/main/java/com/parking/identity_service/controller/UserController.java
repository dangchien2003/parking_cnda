package com.parking.identity_service.controller;

import com.parking.identity_service.dto.request.BlockUserRequest;
import com.parking.identity_service.dto.request.CustomerCreationRequest;
import com.parking.identity_service.dto.request.GoogleAuthenticationRequest;
import com.parking.identity_service.dto.request.StaffCreationRequest;
import com.parking.identity_service.dto.response.ApiResponse;
import com.parking.identity_service.dto.response.DanhSachTaiKhoanResponse;
import com.parking.identity_service.dto.response.UserResponse;
import com.parking.identity_service.dto.response.thong_tin_tai_khoan;
import com.parking.identity_service.entity.User;
import com.parking.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    UserService userService;

    @PostMapping("/customer/registration")
    ApiResponse<UserResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createCustomer(request))
                .build();
    }

    @PostMapping("/customer/registration/google")
    ApiResponse<UserResponse> createCustomer(@Valid @RequestBody GoogleAuthenticationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createCustomerByGoogleAccount(request))
                .build();
    }

    @PostMapping("/staff/registration")
    ApiResponse<UserResponse> createStaff(@Valid @RequestBody StaffCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createStaff(request))
                .build();
    }

    @PatchMapping("/action")
    ApiResponse<List<UserResponse>> blockUser(@Valid @RequestBody BlockUserRequest request) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.blockAccountById(request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<User> getUser(@PathVariable(name = "id") String id) {
        return ApiResponse.<User>builder()
                .result(userService.getUser(id))
                .build();
    }

    @GetMapping("ds-tai-khoan")
    ApiResponse<List<DanhSachTaiKhoanResponse>> layDsTaiKhoan(@RequestParam(name = "name", required = false) String name,
                                                              @RequestParam(name = "status", required = false) String status,
                                                              @RequestParam(name = "page", required = false, defaultValue = "1") int page
    ) {
        return ApiResponse.<List<DanhSachTaiKhoanResponse>>builder()
                .result(userService.layDsTK(name, status, page))
                .build();
    }


    @GetMapping("info/thong_tin_tai_khoan")
    ApiResponse<thong_tin_tai_khoan> thong_tin_tai_khoan(@RequestParam("id") String id) {
        return ApiResponse.<thong_tin_tai_khoan>builder()
                .result(userService.thong_tin_tai_khoan(id))
                .build();
    }

    @GetMapping("internal/get-all-by-emails")
    ApiResponse<List<User>> getListUser(@RequestParam("emails") List<String> emails) {
        return ApiResponse.<List<User>>builder()
                .result(userService.getListUser(emails))
                .build();
    }

}
