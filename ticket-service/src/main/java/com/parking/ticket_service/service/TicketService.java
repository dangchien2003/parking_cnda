package com.parking.ticket_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.ticket_service.dto.request.*;
import com.parking.ticket_service.dto.response.*;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.entity.Plate;
import com.parking.ticket_service.entity.PlateId;
import com.parking.ticket_service.entity.Ticket;
import com.parking.ticket_service.enums.ECategoryUnit;
import com.parking.ticket_service.enums.ECloudinary;
import com.parking.ticket_service.enums.EReason;
import com.parking.ticket_service.enums.UrlPrefixCodeImage;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.mapper.TicketMapper;
import com.parking.ticket_service.repository.CategoryRepository;
import com.parking.ticket_service.repository.PlateRepository;
import com.parking.ticket_service.repository.RedisRepository;
import com.parking.ticket_service.repository.TicketRepository;
import com.parking.ticket_service.repository.httpclient.IdentityClient;
import com.parking.ticket_service.repository.httpclient.VaultClient;
import com.parking.ticket_service.repository.uploader.CloudinaryUploader;
import com.parking.ticket_service.utils.AESUtils;
import com.parking.ticket_service.utils.ENumUtils;
import com.parking.ticket_service.utils.PageUtils;
import com.parking.ticket_service.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TicketService {

    TicketRepository ticketRepository;
    CategoryRepository categoryRepository;
    RedisRepository redisRepository;
    PlateRepository plateRepository;
    PlateCacheService plateCacheService;
    TicketCacheService ticketCacheService;
    TicketMapper ticketMapper;
    VaultClient vaultClient;
    IdentityClient identityClient;
    ObjectMapper objectMapper;
    CloudinaryUploader cloudinaryUploader;
    CategoryService categoryService;
    QRService qrService;


    static final long EXTENDED_UNIT_PRICE_ONE_MINUTE = 1_000;
    static final String KEY_CANCEL_QR = "CANCELED_";

//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
//    public void cancelQr(CancelQrRequest request) throws JsonProcessingException {
//        ContentQr contentQr = getContentQr(request.getQr());
//        validateOwner(contentQr);
//
//        long seconds = (contentQr.getExpireAt() - Instant.now().toEpochMilli()) / 1000;
//
//        if (seconds <= 0)
//            return;
//
//        redisRepository.saveValue(KEY_CANCEL_QR + contentQr.getId(), true, Duration.of(seconds, ChronoUnit.SECONDS));
//    }

    void validateOwner(ContentQr contentQr) {
        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (!contentQr.getUid().equals(uid))
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void checkoutFirstStep(FirstCheckoutRequest request) {

        String ticketId = getContentQr(request.getQr());

        if (!qrService.getNew(ticketId).getContain().equals(request.getQr())) {
            throw new AppException("Mã không còn hiệu lực");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Không tìm thấy vé"));

        long now = Instant.now().toEpochMilli();

        if (ticket.getStartAt() > now) {
            throw new AppException("Vé chưa đến ngày sử dụng");
        }

        if (ticket.getExpireAt() < now) {
            throw new AppException("Vé đã hết hạn");
        }

        List<Plate> plates = plateRepository.findAllById_TicketId(ticketId);
        Plate plate = null;
        if (!plates.isEmpty()) {
            plates.sort(Comparator.comparingInt((Plate item) -> item.getId().getTurn()).reversed());
            plate = plates.getFirst();
            if (plate.getCheckoutAt() != 0) {
                throw new AppException("Cần checkin");
            }
        } else {
            throw new AppException("Cần checkin");
        }

        if (plate.getId().getTurn() != ticket.getTurnTotal()) {
            log.error("Khác lượt sử dụng");
        }

        ticketCacheService.addTicketChecking(ticket.getId());
    }


    public void checkoutSecondStep(SecondCheckoutRequest request) {
        String ticketId = getContentQr(request.getQr());

        if (!ticketCacheService.isTicketChecking(ticketId)) {
            throw new AppException("Không thể checkout");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Không tìm thấy vé"));

        String scanPlate = fakeScanPlate(request.getImage());

        validatePlate(ticket, scanPlate);

        String path = uploadPlate(ticket.getId(), ticket.getTurnTotal() + 1, request.getImage(), "checkin");


        List<Plate> plates = plateRepository.findAllById_TicketId(ticketId);
        Plate plate = null;
        if (!plates.isEmpty()) {
            plates.sort(Comparator.comparingInt((Plate item) -> item.getId().getTurn()).reversed());
            plate = plates.getFirst();
            if (plate.getCheckoutAt() != 0) {
                throw new AppException("Cần checkin");
            }
        } else {
            throw new AppException("Cần checkin");
        }

        long now = Instant.now().toEpochMilli();

        plate.setCheckoutAt(now);
        plate.setImageOut(path);
        plate.setUsedAt(now);

        ticket.setUsedAt(now);

        plateRepository.save(plate);
        ticketRepository.save(ticket);
        ticketCacheService.deleteTicketChecking(ticket.getId());
    }

    public void checkinFirstStep(FirstCheckinRequest request) {
        String ticketId = getContentQr(request.getQr());
        if (!qrService.getNew(ticketId).getContain().equals(request.getQr())) {
            throw new AppException("Mã không còn hiệu lực");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Không tìm thấy vé"));

        long now = Instant.now().toEpochMilli();

        if (ticket.getStartAt() > now) {
            throw new AppException("Vé chưa đến ngày sử dụng");
        }

        if (ticket.getExpireAt() < now) {
            throw new AppException("Vé đã hết hạn");
        }

        List<Plate> plates = plateRepository.findAllById_TicketId(ticketId);
        Plate plate = null;
        if (!plates.isEmpty()) {
            plates.sort(Comparator.comparingInt((Plate item) -> item.getId().getTurn()).reversed());
            plate = plates.getFirst();
            if (plate.getCheckoutAt() == 0) {
                throw new AppException("Vé cần phải checkout");
            }
        }

        if (plate != null && plate.getId().getTurn() != ticket.getTurnTotal()) {
            log.error("Khác lượt sử dụng");
        }

        ticketCacheService.addTicketChecking(ticket.getId());
    }

    public void checkinSecondStep(SecondCheckinRequest request) {

        String ticketId = getContentQr(request.getQr());

        if (!ticketCacheService.isTicketChecking(ticketId)) {
            throw new AppException("Không thể checkin");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Không tìm thấy vé"));

        String scanPlate = fakeScanPlate(request.getImage());

        validatePlate(ticket, scanPlate);

        String path = uploadPlate(ticket.getId(), ticket.getTurnTotal() + 1, request.getImage(), "checkin");

        PlateId plateId = new PlateId(ticket.getId(), ticket.getTurnTotal() + 1);
        Plate newPlate = Plate.builder()
                .id(plateId)
                .urlPrefixCode(UrlPrefixCodeImage.V1.getValue())
                .imageIn(path)
                .checkinAt(Instant.now().toEpochMilli())
                .contentPlate(scanPlate)
                .usedAt(Instant.now().toEpochMilli())
                .build();

        ticket.setTurnTotal(ticket.getTurnTotal() + 1);
        ticket.setUsedAt(Instant.now().toEpochMilli());

        plateRepository.save(newPlate);
        ticketRepository.save(ticket);
        ticketCacheService.deleteTicketChecking(ticket.getId());
    }

    String fakeScanPlate(String image) {
        return "123";
    }

    void validatePlate(Ticket ticket, String scanPlate) {
        if (!Objects.isNull(ticket.getContentPlate()) &&
                !ticket.getContentPlate().equals(scanPlate))
            throw new AppException(ErrorCode.INVALID_PLATE);
    }

    String uploadPlate(String ticket, int turn, String image, String suffix) {
        String nameImage = ticket + "_" + turn + "_" + suffix;
        String folder = ECloudinary.FOLDER_PLATE.getValue();

        try {
            cloudinaryUploader.asyncUploadBase64Image(image, "parking/" + folder, nameImage);
        } catch (IOException e) {
            log.error("cloudinaryUploader error: ", e);
        }

        return folder + '/' + nameImage;
    }

    public List<Ticket> adminBuyForListEmail(AdminBuyTicket request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));

        List<User> users = identityClient.getAllUser(request.getEmails()).getResult();

        List<String> existingEmails = users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        List<String> emailNotExist = request.getEmails().stream()
                .filter(email -> !existingEmails.contains(email))
                .collect(Collectors.toList());

        if (users.isEmpty() || emailNotExist.size() == request.getEmails().size()) {
            throw new AppException("Tất cả email đều không tồn tại");
        }

        List<String> emailBlocked = users.stream()
                .filter(user -> user.getIsBlocked() == 1)
                .map(User::getEmail)
                .collect(Collectors.toList());

        StringBuilder error = new StringBuilder();
        if (!emailBlocked.isEmpty()) {
            error.append("Email đã bị khoá: ")
                    .append(String.join(", ", emailBlocked));
        }

        if (!emailNotExist.isEmpty()) {
            if (error.length() > 0) {
                error.append(" | ");
            }
            error.append("Không tìm thấy tài khoản: ")
                    .append(String.join(", ", emailNotExist));
        }

        if (!error.isEmpty()) {
            throw new AppException(error.toString());
        }
        long from;
        long to;
        try {
            from = TimeUtils.getStartOfDay(request.getStart());
            to = TimeUtils.getEndOfDay(request.getEnd());
        } catch (Exception e) {
            throw new AppException("Định dạng thời gian không hợp lệ");
        }

        if (to <= from) {
            throw new AppException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        if (from < LocalDate.now().atStartOfDay().toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toEpochMilli()) {
            throw new AppException("Ngày bắt đầu không được trong quá khứ");
        }


        int day = calculateDays(from, to);

        List<Ticket> tickets = new ArrayList<>();
        long now = Instant.now().toEpochMilli();
        users.forEach(item -> {
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .uid(item.getUid())
                    .price(day * category.getPrice())
                    .buyAt(now)
                    .expireAt(to)
                    .startAt(from)
                    .category(category)
                    .turnTotal(0)
                    .build();

            tickets.add(ticket);
        });

        ticketRepository.saveAll(tickets);
        return tickets;
    }

    public List<Ticket> tkdsVeBan(String start, String end, String vehicle, int page) {
        long from = TimeUtils.getStartOfDay(start);
        long to = TimeUtils.getEndOfDay(end);

        Pageable pageable = PageUtils.getPageable(page, 30, PageUtils.getSort("ASC", "buyAt"));

        if (!vehicle.isEmpty() & vehicle.equalsIgnoreCase("CAR") && vehicle.equalsIgnoreCase("MOTORBIKE")) {
            throw new AppException("Phương tiện không phù hợp");
        }
        List<Ticket> tickets;
        if (vehicle.isEmpty()) {
            tickets = ticketRepository.findAllByBuyAtBetween(from, to, pageable);
        } else {
            tickets = ticketRepository.findAllByBuyAtBetweenAndCategory_Vehicle(from, to, vehicle.toUpperCase(), pageable);
        }

        return tickets;
    }


    public List<Ticket> getListBetween(String date) {
        String[] split = date.split("/");
        LocalDate firstDayOfMonth = LocalDate.of(Integer.parseInt(split[1]), Integer.parseInt(split[0]), 1);
        long startOfMonth = firstDayOfMonth
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        long endOfMonth = lastDayOfMonth
                .atTime(23, 59, 59, 999_999_999)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return ticketRepository.findAllByBuyAtBetweenOrderByBuyAtAsc(startOfMonth, endOfMonth);
    }

    public List<tkvbResponse> tkVeBan(String date1) {

        List<Ticket> tickets = getListBetween(date1);

        Map<String, tkvbResponse> statisticsByDate = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Ticket ticket : tickets) {
            String date = dateFormat.format(new Date(ticket.getBuyAt()));

            tkvbResponse response = statisticsByDate.getOrDefault(date, new tkvbResponse(date, 0, 0));

            if (ticket.getCategory().getVehicle().equalsIgnoreCase("motorbike")) {
                response.setMotorbike(response.getMotorbike() + 1);
            } else if (ticket.getCategory().getVehicle().equalsIgnoreCase("car")) {
                response.setCar(response.getCar() + 1);
            }

            statisticsByDate.put(date, response);
        }

        return new ArrayList<>(statisticsByDate.values());
    }


    public List<tkdtResponse> tkdt(String date1) {
        List<Ticket> tickets = getListBetween(date1);

        Map<String, int[]> revenueByDate = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Ticket ticket : tickets) {
            String date = dateFormat.format(new Date(ticket.getBuyAt()));
            String vehicle = ticket.getCategory().getVehicle();

            int[] revenue = revenueByDate.computeIfAbsent(date, k -> new int[2]);

            if ("CAR".equalsIgnoreCase(vehicle)) {
                revenue[0] += ticket.getPrice(); // amountCar
            } else {
                revenue[1] += ticket.getPrice(); // amountMotorbike
            }
        }

        List<tkdtResponse> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : revenueByDate.entrySet()) {
            String date = entry.getKey();
            int[] revenue = entry.getValue();
            int amountCar = revenue[0];
            int amountMotorbike = revenue[1];

            result.add(tkdtResponse.builder()
                    .date(date)
                    .amountCar(amountCar)
                    .amountMotorbike(amountMotorbike)
                    .build());
        }

        return result;
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
//    public String renderQr(String ticketId) throws Exception {
//
//        Ticket ticket = getTicket(ticketId);
//
//        if (
//                ticket.getExpireAt() < Instant.now().toEpochMilli())
//            throw new AppException(ErrorCode.TICKET_NO_LONGER_VALID);
//
//        Plate plate = null;
//        if (ticket.getCategory().getUnit().equals(ECategoryUnit.DAY.name()) &&
//                ticket.getTurnTotal() >= ticket.getCategory().getQuantity()) {
//            plate = plateCacheService.getPlate(ticket.getId(), ticket.getTurnTotal());
//
//            if (Objects.isNull(plate) || plate.getCheckoutAt() > 0)
//                throw new AppException(ErrorCode.TICKET_NO_LONGER_VALID);
//        }
//
//        ContentQr contentQr = ticketMapper.toContentQr(ticket);
//        contentQr.setId(UUID.randomUUID().toString());
//
//        byte[] compressedData = objectMapper.writeValueAsString(contentQr).getBytes();
//        return AESUtils.encrypt(compressedData);
//    }

    String getContentQr(String encrypt) {
        try {
            byte[] decryptedCompressedData = AESUtils.decrypt(encrypt);
            return new String(decryptedCompressedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AppException(ErrorCode.TICKET_NOTFOUND);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public TicketResponse getInfoTicket(String ticketId) {
        Ticket ticket = getTicket(ticketId);

        if (ticket == null)
            throw new AppException(ErrorCode.TICKET_NOTFOUND);

        TicketResponse ticketResponse = ticketMapper.toTicketResponse(ticket);
        ticketResponse.setBuyTime(TimeUtils.convertTime(ticket.getBuyAt(), "HH:mm dd/MM/yyyy"));
        ticketResponse.setExpireTime(TimeUtils.convertTime(ticket.getExpireAt(), "HH:mm dd/MM/yyyy"));
        ticketResponse.setStartTime(TimeUtils.convertTime(ticket.getStartAt(), "HH:mm dd/MM/yyyy"));
        if (ticket.getUsedAt() != 0) {
            ticketResponse.setUsedTime(TimeUtils.convertTime(ticket.getUsedAt(), "HH:mm dd/MM/yyyy"));
        }
        return ticketResponse;
    }


    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public TicketResponse getInfoTicketADMIN(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    throw new AppException(ErrorCode.TICKET_NOTFOUND);
                });

        TicketResponse ticketResponse = ticketMapper.toTicketResponse(ticket);
        ticketResponse.setBuyTime(TimeUtils.convertTime(ticket.getBuyAt(), "HH:mm dd/MM/yyyy"));
        ticketResponse.setExpireTime(TimeUtils.convertTime(ticket.getExpireAt(), "HH:mm dd/MM/yyyy"));
        ticketResponse.setStartTime(TimeUtils.convertTime(ticket.getStartAt(), "HH:mm dd/MM/yyyy"));
        if (ticket.getUsedAt() != 0) {
            ticketResponse.setUsedTime(TimeUtils.convertTime(ticket.getUsedAt(), "HH:mm dd/MM/yyyy"));
        }
        ticketResponse.setPlate(ticket.getContentPlate() == null ? "" : ticket.getContentPlate());
        ticketResponse.setUnit(ticket.getCategory().getUnit());
        ticketResponse.setEmail(identityClient.getInfoUser(ticket.getUid()).getResult().getEmail());
        return ticketResponse;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public void extendTicket(String ticketId, String date) {

        Ticket ticket = getTicket(ticketId);

        Plate plate = plateCacheService.getPlate(ticket.getId(), ticket.getTurnTotal());
        if (Objects.isNull(plate) || plate.getCheckoutAt() > 0)
            throw new AppException(ErrorCode.UNUSED_TICKET);

        if (ticket.getExpireAt() > Instant.now().toEpochMilli() ||
                ticket.getTurnTotal() == 0)
            throw new AppException(ErrorCode.EXTEND_FAIL);

        long newExpire = TimeUtils.timeToLong("23:59:59 " + date, "HH:mm:ss dd/MM/yyyy");


        int days = calculateDays(ticket.getExpireAt(), newExpire);

        long amount = days * ticket.getCategory().getPrice() * 2;
        BalenceResponse balance = vaultClient.getBalance().getResult();

        if (balance.getBalence() < amount)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);


        AddFluctuationRequest addFluctuationRequest = AddFluctuationRequest.builder()
                .amount((int) amount)
                .objectId(ticket.getId())
                .build();

        vaultClient.addFluctuation(addFluctuationRequest, EReason.EXTEND_TICKET.name());

        // gửi thông báo

        ticket.setExpireAt(newExpire);
        ticketRepository.save(ticket);
    }

    Ticket getTicket(String ticketId) {
        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ticketCacheService.getTicket(ticketId, uid);
    }

    Ticket getTicket(String ticketId, String uid) {
        return Objects.isNull(uid)
                ? ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED))
                : ticketRepository.findByIdAndUid(ticketId, uid)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
//    public void cancel(String ticketId) {
//
//        Ticket ticket = getTicket(ticketId);
//
//        if (ticket.getTurnTotal() > 0 ||
//                ticket.getCancleAt() > 0 ||
//                !Objects.isNull(ticket.getContentPlate()))
//            throw new AppException(ErrorCode.CANNOT_CANCEL_TICKET);
//
//
//        AddFluctuationRequest addFluctuationRequest = AddFluctuationRequest.builder()
//                .amount(ticket.getCategory().getPrice())
//                .objectId(ticketId)
//                .build();
//        vaultClient.addFluctuation(addFluctuationRequest, EReason.CANCEL_TICKET.name());
//
//        ticket.setCancleAt(Instant.now().toEpochMilli());
//        ticketRepository.save(ticket);
//    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public TicketResponse updatePlate(TicketUpdatePlateRequest request) {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        Ticket ticket = getTicket(request.getTicketId());

        if (!ticket.getUid().equalsIgnoreCase(uid))
            throw new AppException(ErrorCode.TICKET_NOTFOUND);

        if ((ticket.getTurnTotal() > 0 && !Objects.isNull(ticket.getContentPlate()))
        )
            throw new AppException(ErrorCode.CANNOT_UPDATE_PLATE);

        ticket.setContentPlate(request.getPlate());

        return ticketMapper.toTicketResponse(ticketRepository.save(ticket));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public TicketResponse buy(BuyTicketRequest request) {

        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Category category = categoryRepository.findById(request.getCategory())
                .orElse(null);

        if (category.getStatus().equalsIgnoreCase("INACTIVE"))
            throw new AppException("Không tìm thấy vé");

        long start = TimeUtils.timeToLong("00:00:00 " + request.getStartDate(), "HH:mm:ss dd/MM/yyyy");
        long end = TimeUtils.timeToLong("23:59:59 " + request.getEndDate(), "HH:mm:ss dd/MM/yyyy");

        if (end <= start) {
            throw new AppException("Ngày kết thúc phải sau ngày hiện tại");
        }

        if (start < LocalDate.now().atStartOfDay().toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toEpochMilli()) {
            throw new AppException("Ngày bắt đầu không được trong quá khứ");
        }

        long startCurrentDay = getStartOfDayInMillis();
        if (start < startCurrentDay)
            throw new AppException("Thời gian bắt đầu không hợp lệ");

        if (start > plus7days(startCurrentDay))
            throw new AppException("Chỉ được đặt trước 7 ngày");

        int days = calculateDays(start, end);
        if (days > 30)
            throw new AppException("Hiệu lực vé tối đa là 30 ngày");

        int balance = vaultClient.getBalance().getResult().getBalence();

        int price = days * category.getPrice();
        if (balance < price)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);

        List<EmptyPositionResponse> tickets = categoryService.getEmptyPosition(request.getStartDate(), request.getCategory());

        tickets.forEach(item -> {
            if (item.getQuantity() <= 0) {
                throw new AppException("Ngày " + item.getDate() + " đã hết vị trí trống");
            }
        });

        Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID().toString())
                .uid(uid)
                .price(price)
                .category(category)
                .buyAt(Instant.now().toEpochMilli())
                .startAt(start)
                .expireAt(end)
                .build();

        vaultClient.addFluctuation(
                AddFluctuationRequest.builder()
                        .amount(price)
                        .objectId(ticket.getId())
                        .build()
                , EReason.BUY_TICKET.name());

        ticket = ticketRepository.save(ticket);

        qrService.add(ticket.getId());
        return ticketMapper.toTicketResponse(ticket);
    }

    long plus7days(long start) {
        LocalDate startDate = Instant.ofEpochMilli(start)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dateAfter30Days = startDate.plusDays(7);

        LocalDateTime endOfDay = dateAfter30Days.atTime(LocalTime.MAX);

        return endOfDay.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    long getStartOfDayInMillis() {
        // Lấy ngày hiện tại
        LocalDate currentDate = LocalDate.now();

        // Chuyển đổi ngày hiện tại thành LocalDateTime lúc 00:00:00
        LocalDateTime startOfDay = currentDate.atStartOfDay();

        // Chuyển LocalDateTime thành Instant, sau đó chuyển sang mili giây
        return startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static int calculateDays(long start, long end) {
        // Một ngày có 86400000 mili giây (24 giờ * 60 phút * 60 giây * 1000 mili giây)
        long oneDayInMillis = TimeUnit.DAYS.toMillis(1);

        // Nếu start == end, rõ ràng chỉ có 1 ngày
        if (start == end) {
            return 1;
        }

        // Nếu start < end, tính số ngày giữa start và end
        long diffInMillis = end - start;

        // Chia chênh lệch thời gian cho số mili giây trong 1 ngày, rồi làm tròn lên
        long diffInDays = diffInMillis / oneDayInMillis;

        // Nếu phần còn lại (mod) > 0, tức là thời gian nằm qua một ngày khác, cần cộng thêm 1 ngày
        if (diffInMillis % oneDayInMillis > 0) {
            diffInDays++;
        }

        return (int) diffInDays;
    }

    long getExpireTicket(String unit) {

        ECategoryUnit typeUnit;
        try {
            typeUnit = ENumUtils.getType(ECategoryUnit.class, unit);
        } catch (AppException e) {
            throw new AppException(ErrorCode.CANNOT_BUY_TICKET);
        }

        ChronoUnit chronoUnit;
        switch (typeUnit) {
            case DAY, TIMES -> chronoUnit = ChronoUnit.DAYS;
            case WEEK -> chronoUnit = ChronoUnit.WEEKS;
            case MONTH -> chronoUnit = ChronoUnit.MONTHS;
            default -> throw new AppException(ErrorCode.NOTFOUND_CATEGORY_UNIT);
        }

        return Instant.now().plus(1, chronoUnit).toEpochMilli();
    }

    public List<TicketResponse> getAll(int page, String vehicle) {
        int pageSize = 20;

        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        Pageable pageable = PageUtils.getPageable(page, pageSize, PageUtils.getSort("ESC", "buyAt"));
        Page<Ticket> pageData;

        if (vehicle.equalsIgnoreCase("all")) {
            pageData = ticketRepository.findByUid(uid, pageable);
        } else if (vehicle.equalsIgnoreCase("car")) {
            pageData = ticketRepository.findByUidAndCategory_Vehicle(uid, "CAR", pageable);
        } else {
            pageData = ticketRepository.findByUidAndCategory_Vehicle(uid, "MOTORBIKE", pageable);
        }

        return pageData.getContent().stream().map(ticket -> {
            TicketResponse ticketResponse = ticketMapper.toTicketResponse(ticket);
            if (ticket.getStartAt() > Instant.now().toEpochMilli()) {
                ticketResponse.setStatus("Chờ sử dụng");
            } else if (ticket.getExpireAt() < Instant.now().toEpochMilli()) {
                ticketResponse.setStatus("Đã hết hạn");
            } else {
                ticketResponse.setStatus("Đang sử dụng");
            }

            ticketResponse.setStartTime(TimeUtils.convertTime(ticket.getStartAt(), "dd/MM/yyyy"));

            return ticketResponse;
        }).toList();
    }

    public Integer countTicketPurchased(String uid) {
        if (uid == null) {
            uid = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return ticketRepository.countByUid(uid);
    }

    public Integer countUseTimesInMonth() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        long end = Instant.now().toEpochMilli();

        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        ZonedDateTime startOfMonth = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault());
        long start = startOfMonth.toInstant().toEpochMilli();

        List<Ticket> ticketsUseInMonth = ticketRepository.findAllByUidAndUsedAtBetween(uid, start, end);

        List<String> ticketIds = new ArrayList<>();
        ticketsUseInMonth.forEach(ticket -> {
            ticketIds.add(ticket.getId());
        });

        return plateRepository.countByTicketIds(ticketIds, start, end);
    }

    public int getTotalTurn() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();


        List<Ticket> tickets = ticketRepository.findAllByUidAndTurnTotalGreaterThan(uid, 0);
        int sum = 0;
        for (Ticket ticket : tickets) {
            sum += ticket.getTurnTotal();
        }

        return sum;
    }

    public List<RecentActivityResponse> getRecentActivity() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Ticket> tickets = ticketRepository.findByUidAndTurnTotalGreaterThan(uid, 0, PageUtils.getPageable(1, 5, PageUtils.getSort("DESC", "usedAt")));

        return tickets.stream().map(ticket -> new RecentActivityResponse(ticket.getCategory().getName(), ticket.getTurnTotal(), TimeUtils.convertTime(ticket.getUsedAt(), "dd/MM/yyyy hh:mm:ss"))).toList();
    }

}
