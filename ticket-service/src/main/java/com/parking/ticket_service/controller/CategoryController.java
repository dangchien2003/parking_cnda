package com.parking.ticket_service.controller;

import com.parking.ticket_service.dto.request.CategoryCreatitonRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStationRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStatusRequest;
import com.parking.ticket_service.dto.response.ApiResponse;
import com.parking.ticket_service.dto.response.CategoryResponse;
import com.parking.ticket_service.dto.response.EmptyPositionResponse;
import com.parking.ticket_service.dto.response.ManagerDetailCategoryResponse;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.service.CategoryService;
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
@RequestMapping("/category")
public class CategoryController {

    CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreatitonRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    @PatchMapping
    ApiResponse<CategoryResponse> update(@Valid @RequestBody CategoryUpdateRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.update(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<ManagerDetailCategoryResponse>> findAll(
            @RequestParam(name = "status", required = false, defaultValue = "")
            String status,

            @RequestParam(name = "page", required = false, defaultValue = "1")
            @Min(value = 1)
            int page,

            @RequestParam(name = "sort", required = false, defaultValue = "desc")
            String sort,

            @RequestParam(name = "field", required = false, defaultValue = "CreateAt")
            String field
    ) {
        return ApiResponse.<List<ManagerDetailCategoryResponse>>builder()
                .result(categoryService.findAll(status, page, sort, field))
                .build();
    }

    @PutMapping("/update/station")
    ApiResponse<Category> update(@Valid @RequestBody CategoryUpdateStationRequest request) {
        return ApiResponse.<Category>builder()
                .result(categoryService.update(request))
                .build();
    }

    @PatchMapping("/update/status")
    ApiResponse<Void> update(@Valid @RequestBody CategoryUpdateStatusRequest request) {
        categoryService.updateStatus(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/info/{id}")
    ApiResponse<CategoryResponse> endpoint(@PathVariable(name = "id") String id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getInfo(id))
                .build();
    }

    @GetMapping("/find/all")
    ApiResponse<List<CategoryResponse>> getCategory(@RequestParam(name = "page", defaultValue = "1") int page) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.find(page))
                .build();
    }

    @GetMapping("/detail/{id}")
    ApiResponse<Category> managerGetCategory(@PathVariable(name = "id") String id) {
        return ApiResponse.<Category>builder()
                .result(categoryService.managerGetCategory(id))
                .build();
    }

    @GetMapping("empty-position")
    ApiResponse<List<EmptyPositionResponse>> getPositionEmpty(@RequestParam(name = "start") String startDate,
                                                              @RequestParam(name = "ticket") String category) {
        return ApiResponse.<List<EmptyPositionResponse>>builder()
                .result(categoryService.getEmptyPosition(startDate, category))
                .build();
    }
}
