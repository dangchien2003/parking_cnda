package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.request.CategoryCreatitonRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStationRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStatusRequest;
import com.parking.ticket_service.dto.response.CategoryResponse;
import com.parking.ticket_service.dto.response.PageResponse;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.entity.CategoryHistory;
import com.parking.ticket_service.entity.Station;
import com.parking.ticket_service.enums.AmountPage;
import com.parking.ticket_service.enums.ECategoryStatus;
import com.parking.ticket_service.enums.ECategoryUnit;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.mapper.CategoryHistoryMapper;
import com.parking.ticket_service.mapper.CategoryMapper;
import com.parking.ticket_service.repository.CategoryHistoryRepository;
import com.parking.ticket_service.repository.CategoryRepository;
import com.parking.ticket_service.repository.StationRepository;
import com.parking.ticket_service.utils.ENumUtils;
import com.parking.ticket_service.utils.FieldCheckers;
import com.parking.ticket_service.utils.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CategoryService {

    CategoryRepository categoryRepository;
    CategoryHistoryRepository categoryHistoryRepository;
    StationRepository stationRepository;
    CategoryMapper categoryMapper;
    CategoryHistoryMapper categoryHistoryMapper;

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public CategoryResponse create(CategoryCreatitonRequest request) {

        long now = Instant.now().toEpochMilli();
        Category category = categoryMapper.toCategory(request);
        category.setStatus(ECategoryStatus.INACTIVE.name());
        category.setUnit(ENumUtils
                .getType(ECategoryUnit.class, request.getUnit())
                .name());
        category.setCreateAt(now);
        category.setModifiedAt(now);

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public CategoryResponse update(CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        categoryMapper.toCategory(request, category);
        category.setModifiedAt(Instant.now().toEpochMilli());
        category = categoryRepository.save(category);

        saveHistory(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public PageResponse<Category> findAll(String type, int page, String sort, String field) {

        if (!FieldCheckers.hasField(Category.class, field))
            field = "createAt";

        ECategoryStatus status;
        try {
            status = ENumUtils.getType(ECategoryStatus.class, type);
        } catch (AppException e) {
            throw new AppException(ErrorCode.NOTFOUND_URL);
        }

        Pageable pageable = PageUtils
                .getPageable(page, AmountPage.FIND_CATEGORY.getAmount(), PageUtils.getSort(sort, field));

        Page<Category> pageData = categoryRepository.findAllByStatus(status.name(), pageable);
        return PageResponse.<Category>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .data(pageData.stream().toList())
                .build();


    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public Category update(CategoryUpdateStationRequest request) {

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        List<Station> stations = stationRepository.findAllById(Arrays.asList(request.getStations()));

        if (stations.isEmpty()) {
            throw new AppException(ErrorCode.STATION_NOT_FOUND);
        }

//        category.setStations(new HashSet<>(stations));
        category.setModifiedAt(Instant.now().toEpochMilli());
        return categoryRepository.save(category);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public void updateStatus(CategoryUpdateStatusRequest request) {

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        String categoryStatus = ENumUtils
                .getType(ECategoryStatus.class, request.getStatus())
                .name();

        if (category.getStatus().equals(categoryStatus))
            throw new AppException(ErrorCode.UPDATE_FAIL);

        category.setStatus(categoryStatus);
        category.setModifiedAt(Instant.now().toEpochMilli());
        category = categoryRepository.save(category);

        saveHistory(category);
    }

    public CategoryResponse getInfo(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.DATA_NOT_FOUND));
        return convert(category);
    }

    public List<CategoryResponse> find(String vehicle,int page) {
        Page<Category> data = categoryRepository.findAllByVehicle(vehicle, PageUtils.getPageable(page, 10, PageUtils.getSort("ASC", "unit")));
        return data.getContent().stream().map(this::convert).toList();
    }

    CategoryResponse convert(Category category) {
        String duration;
        String usage = "Vô hạn";
        String unit =  category.getUnit().toUpperCase();
        if(unit.equals(ECategoryUnit.TIMES.name())) {
            duration = "24 giờ từ khi sử dụng";
            usage = category.getQuantity() + " lần";
        }else if(unit.equals(ECategoryUnit.DAY.name())){
            duration = "24 giờ từ lúc mua";
        }else if(unit.equals(ECategoryUnit.MONTH.name())){
            duration = "1 tháng từ lúc mua";
        }else if(unit.equals(ECategoryUnit.WEEK.name())){
            duration = "1 tuần từ lúc mua";
        }else {
            duration = "";
        }

        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);
        categoryResponse.setDuration(duration);
        categoryResponse.setUsage(usage);
        return categoryResponse;
    }

    void saveHistory(Category category) {
        CategoryHistory categoryHistory = categoryHistoryMapper.toCategoryHistory(category);
        categoryHistory.setCreateAt(Instant.now().toEpochMilli());
        categoryHistoryRepository.save(categoryHistory);
    }
}
