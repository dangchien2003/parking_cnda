package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.request.CategoryCreatitonRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStationRequest;
import com.parking.ticket_service.dto.request.CategoryUpdateStatusRequest;
import com.parking.ticket_service.dto.response.CategoryResponse;
import com.parking.ticket_service.dto.response.EmptyPositionResponse;
import com.parking.ticket_service.dto.response.ManagerDetailCategoryResponse;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.entity.Setting;
import com.parking.ticket_service.entity.Station;
import com.parking.ticket_service.entity.Ticket;
import com.parking.ticket_service.enums.AmountPage;
import com.parking.ticket_service.enums.ECategoryStatus;
import com.parking.ticket_service.enums.ECategoryUnit;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.mapper.CategoryMapper;
import com.parking.ticket_service.repository.CategoryRepository;
import com.parking.ticket_service.repository.SettingRepository;
import com.parking.ticket_service.repository.StationRepository;
import com.parking.ticket_service.repository.TicketRepository;
import com.parking.ticket_service.utils.ENumUtils;
import com.parking.ticket_service.utils.FieldCheckers;
import com.parking.ticket_service.utils.PageUtils;
import com.parking.ticket_service.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.EnumUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CategoryService {
    CategoryRepository categoryRepository;
    TicketRepository ticketRepository;
    StationRepository stationRepository;
    CategoryMapper categoryMapper;
    SettingRepository settingRepository;

    public CategoryResponse create(CategoryCreatitonRequest request) {

        long now = Instant.now().toEpochMilli();

        Category category = categoryRepository.findById(request.getId()).orElse(null);

        if (category != null) {
            throw new AppException("Vé đã tồn tại");
        }

        if (!request.getVehicle().equalsIgnoreCase("CAR") &&
                !request.getVehicle().equalsIgnoreCase("MOTORBIKE")) {
            throw new AppException("Phương tiện không hỗ trợ");
        }

        category = categoryMapper.toCategory(request);
        category.setVehicle(request.getVehicle().toUpperCase());
        try {
            category.setStatus(ECategoryStatus.valueOf(request.getStatus()).name());
        } catch (Exception e) {
            throw new AppException("Trạng thái phải là: ACTIVE hoặc INACTIVE");
        }

        try {
            category.setUnit(ENumUtils
                    .getType(ECategoryUnit.class, request.getUnit())
                    .name());
        } catch (Exception e) {
            throw new AppException("Đơn vị không nằm trong: TIMES, DAY, WEEK, MONTH");
        }
        category.setCreateAt(now);
        category.setModifiedAt(now);


        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse update(CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        categoryMapper.toCategory(request, category);
        category.setModifiedAt(Instant.now().toEpochMilli());
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public List<ManagerDetailCategoryResponse> findAll(String status, int page, String sort, String field) {

        if (!FieldCheckers.hasField(Category.class, field))
            field = "createAt";

        ECategoryStatus eStatus;
        try {
            eStatus = ENumUtils.getType(ECategoryStatus.class, status);
        } catch (AppException e) {
            throw new AppException(ErrorCode.INVALID_DATA);
        }

        Pageable pageable = PageUtils
                .getPageable(page, AmountPage.FIND_CATEGORY.getAmount(), PageUtils.getSort(sort, field));

        Page<Category> pageData = categoryRepository.findAllByStatus(eStatus.name(), pageable);

        return pageData.getContent().stream().map(item -> {
            ManagerDetailCategoryResponse record = new ManagerDetailCategoryResponse();
            record.setId(item.getId());
            record.setName(item.getName());
            record.setVehicle(item.getVehicle());
            record.setType(convertToType(item.getUnit()));
            record.setTimeEnd(convertToTimeEnd(item.getUnit(), item.getQuantity()));
            record.setPrice(item.getPrice());
            record.setStatus(item.getStatus());
            return record;
        }).toList();
    }

    String convertToTimeEnd(String unit, int quantity) {
        ECategoryUnit eUnit = EnumUtils.findEnumInsensitiveCase(ECategoryUnit.class, unit);
        switch (eUnit) {
            case DAY -> {
                return quantity * 24 + " giờ";
            }
            case TIMES -> {
                return quantity * 24 + " giờ";
            }
            case WEEK -> {
                return quantity * 24 * 7 + " giờ";
            }
            case MONTH -> {
                return quantity * 24 * 30 + " giờ";
            }
            default -> {
                return "...";
            }
        }
    }

    String convertToType(String unit) {
        ECategoryUnit eUnit = EnumUtils.findEnumInsensitiveCase(ECategoryUnit.class, unit);
        switch (eUnit) {
            case DAY -> {
                return "Vé ngày";
            }
            case TIMES -> {
                return "Vé lượt";
            }
            case WEEK -> {
                return "Vé tuần";
            }
            case MONTH -> {
                return "Vé tháng";
            }
            default -> {
                return unit;
            }
        }
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
    }

    public CategoryResponse getInfo(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        return convert(category);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public Category managerGetCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        return category;
    }

    public List<CategoryResponse> find(int page) {

        Page<Category> data = categoryRepository.findAllByStatus(ECategoryStatus.ACTIVE.name(), PageUtils.getPageable(page, 10, PageUtils.getSort("ASC", "vehicle")));
        return data.getContent().stream().map(this::convert).toList();
    }

    CategoryResponse convert(Category category) {
        String usage = "Vô hạn";
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);
        categoryResponse.setDuration("23:59");
        categoryResponse.setUsage(usage);
        categoryResponse.setVehicle(category.getVehicle());
        return categoryResponse;
    }

    long plus30days(long start) {
        LocalDate startDate = Instant.ofEpochMilli(start)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dateAfter30Days = startDate.plusDays(30);

        LocalDateTime endOfDay = dateAfter30Days.atTime(LocalTime.MAX);

        return endOfDay.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public List<EmptyPositionResponse> getEmptyPosition(String startDate, String category) {
        Category categoryInDB = categoryRepository.findById(category)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));

        long startAt = TimeUtils.timeToLong("00:00:00 " + startDate, "HH:mm:ss dd/MM/yyyy");
        long endAt = plus30days(startAt);

        List<Ticket> tickets = ticketRepository.findTickets(
                startAt, endAt, startAt, endAt, categoryInDB.getVehicle());

        Setting setting = getNewRecord();
        int max = 0;
        if (categoryInDB.getVehicle().toUpperCase().equals("CAR")) {
            max = setting.getMaxPositionCar() - setting.getSpareCar();
        } else {
            max = setting.getMaxPositionMotorbike() - setting.getSpareMotorbike();
        }

        return calcEmptyPosition(tickets, startAt, endAt, max);
    }

    public List<CategoryResponse> timKiemVe(String vehicle, String status) {
        if (!vehicle.equalsIgnoreCase("CAR") && !vehicle.equalsIgnoreCase("MOTORBIKE")) {
            throw new AppException("Phương tiện không xác định");
        }
        ECategoryStatus categoryStatus;

        try {
            categoryStatus = ECategoryStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new AppException("Trạng thái không phù hợp");
        }

        return categoryRepository.findAllByVehicleAndStatus(vehicle.toUpperCase(), categoryStatus.name())
                .stream().map(item -> {
                    CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(item);
                    categoryResponse.setDuration("Tuỳ chỉnh thời gian sử dụng");
                    categoryResponse.setUsage("Không giới hạn số lần");
                    categoryResponse.setUnit("Vé ngày");
                    categoryResponse.setVehicle(item.getVehicle().equalsIgnoreCase("CAR") ? "Ô tô" : "Xe máy");
                    return categoryResponse;
                }).toList();
    }

    public List<EmptyPositionResponse> calcEmptyPosition(List<Ticket> tickets, long startAt, long endAt, int max) {

        Map<String, Integer> dateQuantityMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Ticket ticket : tickets) {
            long ticketStartAt = ticket.getStartAt();
            long ticketExpireAt = ticket.getExpireAt();

            // Convert startAt and expireAt to Calendar for easier iteration
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(ticketStartAt);
            while (calendar.getTimeInMillis() <= ticketExpireAt) {
                String date = dateFormat.format(calendar.getTime());

                dateQuantityMap.put(date, dateQuantityMap.getOrDefault(date, 0) + 1);

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        List<EmptyPositionResponse> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startAt);

        while (calendar.getTimeInMillis() <= endAt) {
            String date = dateFormat.format(calendar.getTime());

            EmptyPositionResponse response = new EmptyPositionResponse();
            response.setDate(date);

            int quantity = dateQuantityMap.getOrDefault(date, 0);
            int remaining = max - quantity;
            response.setQuantity(remaining);

            result.add(response);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Collections.sort(result, new Comparator<EmptyPositionResponse>() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public int compare(EmptyPositionResponse o1, EmptyPositionResponse o2) {
                try {
                    Date date1 = sdf.parse(o1.getDate());
                    Date date2 = sdf.parse(o2.getDate());
                    return date1.compareTo(date2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        return result;
    }

    Setting getNewRecord() {
        List<Setting> settings = settingRepository.findAllByOrderByIdDesc();
        if (settings.size() == 0) {
            return new Setting();
        }

        return settings.getFirst();
    }
}
