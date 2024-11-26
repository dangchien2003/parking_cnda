package com.parking.vault_service.service;

import com.parking.vault_service.dto.request.AddFluctuationRequest;
import com.parking.vault_service.dto.response.Fluctuation30DaysResponse;
import com.parking.vault_service.dto.response.FluctuationResponse;
import com.parking.vault_service.dto.response.PageResponse;
import com.parking.vault_service.entity.Fluctuation;
import com.parking.vault_service.entity.Owner;
import com.parking.vault_service.enums.EPageQuantity;
import com.parking.vault_service.enums.EReason;
import com.parking.vault_service.enums.ETransaction;
import com.parking.vault_service.exception.AppException;
import com.parking.vault_service.exception.ErrorCode;
import com.parking.vault_service.repository.FluctuationRepository;
import com.parking.vault_service.repository.OwnerRepository;
import com.parking.vault_service.utils.ENumUtil;
import com.parking.vault_service.utils.PageUtil;
import com.parking.vault_service.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FluctuationService {

    FluctuationRepository fluctuationRepository;
    OwnerRepository ownerRepository;
    static final List<EReason> REASONS_CREDIT = List.of(EReason.APPROVE, EReason.CANCEL_TICKET);

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public void addFluctuation(AddFluctuationRequest request, String reason) {
        EReason eReason;
        try {
            eReason = ENumUtil.getType(EReason.class, reason);
        } catch (AppException e) {
            throw new AppException(ErrorCode.NOTFOUND_URL);
        }

        String description = eReason.getValue() + "-" + request.getObjectId();

        ETransaction eTransaction = REASONS_CREDIT.contains(eReason)
                ? ETransaction.CREDIT : ETransaction.DEBIT;

        addFluctuation(request.getAmount(), eTransaction, eReason, description);
    }

    Fluctuation addFluctuation(int amount, @NotNull ETransaction transaction, EReason reason, String description) {
        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Owner owner = ownerRepository.findById(uid)
                .orElseThrow(() -> new AppException(ErrorCode.OWNER_NOT_EXIST));

        if (transaction.equals(ETransaction.CREDIT)) {
            owner.setBalance(owner.getBalance() + amount);
        } else if (transaction.equals(ETransaction.DEBIT)) {
            owner.setBalance(owner.getBalance() - amount);
        } else {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        Fluctuation fluctuation = Fluctuation.builder()
                .id(UUID.randomUUID().toString())
                .ownerId(owner.getId())
                .amount(amount)
                .transaction(transaction.getValue())
                .reason(reason.getValue())
                .createAt(Instant.now().toEpochMilli())
                .description(description)
                .build();


        fluctuation = fluctuationRepository.save(fluctuation);
        ownerRepository.save(owner);
        return fluctuation;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public PageResponse<Fluctuation> getAllByStaff(String type, int page, String sort, String field) {
        return getAll(type, null, page, sort, field);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public PageResponse<Fluctuation> getAllByStaff(String type, String uid, int page, String sort, String field) {
        return getAll(type, uid, page, sort, field);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public List<FluctuationResponse> getAllByCustomer(String type, String date, int page, String sort, String field) {

        long start = 0;
        long end = 0;
        if (date != null) {
            start = TimeUtils.timeToLong(date, "dd/MM/yyyy");
            end = start + 24 * 60 * 60 * 1000 - 1;
        }

        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<Fluctuation> fluctuations = null;

        if (start != 0 && !type.isEmpty()) {
            fluctuations = fluctuationRepository.findAllByOwnerIdAndReasonAndCreateAtBetweenOrderByCreateAtDesc(
                    uid, type, start, end, PageUtil.getPageable(page, 20, sort, field));
        } else if (start != 0) {
            fluctuations = fluctuationRepository.findAllByOwnerIdAndCreateAtBetweenOrderByCreateAtDesc(
                    uid, start, end, PageUtil.getPageable(page, 20, sort, field));
        } else if (!type.isEmpty()) {
            fluctuations = fluctuationRepository.findAllByOwnerIdAndReasonOrderByCreateAtDesc(
                    uid, type, PageUtil.getPageable(page, 20, sort, field));
        } else {
            fluctuations = fluctuationRepository.findAllByOwnerIdOrderByCreateAtDesc(
                    uid, PageUtil.getPageable(page, 20, sort, field));
        }

        return fluctuations.stream().map(fluctuation -> {
            FluctuationResponse fluctuationResponse = new FluctuationResponse();
            fluctuationResponse.setTime(TimeUtils.convertTime(fluctuation.getCreateAt(), "dd/MM/yyyy HH:mm:ss"));
            fluctuationResponse.setAmount(fluctuation.getAmount());
            fluctuationResponse.setContain(fluctuation.getReason());
            return fluctuationResponse;
        }).toList();
    }

    PageResponse<Fluctuation> getAll(String type, String uid, int page, String sort, String field) {
        EReason reason = null;
        Pageable pageable = PageUtil.getPageable(page, EPageQuantity.FLUCTUATION.getQuantity(), sort, field);

        Page<Fluctuation> pageData;
        if (!type.equalsIgnoreCase("OTHER")) {
            try {
                reason = ENumUtil.getType(EReason.class, type);
            } catch (AppException e) {
                throw new AppException(ErrorCode.NOTFOUND_URL);
            }
        }

        Owner owner = null;
        if (!Objects.isNull(uid)) {
            owner = ownerRepository.findById(uid)
                    .orElseThrow(() -> new AppException(ErrorCode.OWNER_NOT_EXIST));
        }

        if (!Objects.isNull(owner)) {
            if (Objects.isNull(reason)) {
                pageData = fluctuationRepository.findAllByReasonNotInAndOwnerId(getAllTypeReason(), owner.getId(), pageable);
            } else if (type.equalsIgnoreCase("ALL")) {
                pageData = fluctuationRepository.findAllByOwnerId(owner.getId(), pageable);
            } else {
                pageData = fluctuationRepository.findAllByReasonAndOwnerId(reason.getValue(), owner.getId(), pageable);
            }
        } else {
            if (Objects.isNull(reason)) {
                pageData = fluctuationRepository.findAllByReasonNotIn(getAllTypeReason(), pageable);
            } else {
                pageData = fluctuationRepository.findAllByReason(reason.getValue(), pageable);
            }

        }


        return PageResponse.<Fluctuation>builder()
                .pageSize(pageData.getSize())
                .currentPage(page)
                .data(pageData.getContent())
                .build();
    }

    public Integer getUseInMonth() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        long now = Instant.now().toEpochMilli();
        long start = TimeUtils.getStartOfMonth(TimeUtils.convertTime(now, "dd/MM/yyyy"));

        List<Fluctuation> fluctuations = fluctuationRepository
                .findByTransactionAndOwnerIdAndCreateAtIsBetween(ETransaction.DEBIT.name(), uid, start, now);

        int total = 0;
        for (Fluctuation fluctuation : fluctuations) {
            total += fluctuation.getAmount();
        }

        return total;
    }

    public List<Fluctuation30DaysResponse> fluctuationIn30Days() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate now = LocalDate.now();
        long end = Instant.now().toEpochMilli();
        String currentDay = TimeUtils.convertTime(end, "dd/MM/yyyy");
        long start = now.minusDays(30).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<Fluctuation> fluctuations = fluctuationRepository.findAllByOwnerIdAndCreateAtBetweenOrderByCreateAtDesc(uid, start, end);

        Owner owner = ownerRepository.findById(uid).orElseThrow(() -> new AppException(ErrorCode.OWNER_NOT_EXIST));

        int balance = owner.getBalance();

        List<Fluctuation30DaysResponse> data = new LinkedList<>();
        data.addFirst(new Fluctuation30DaysResponse(currentDay, owner.getBalance()));
        String day = null;
        for (Fluctuation fluctuation : fluctuations) {
            String createDay = TimeUtils.convertTime(fluctuation.getCreateAt(), "dd/MM/yyyy");

            if (day == null) {
                day = createDay;
            }

            if (!day.equals(createDay)) {
                day = createDay;
                Fluctuation30DaysResponse fluctuation30DaysResponse = Fluctuation30DaysResponse.builder()
                        .day(day)
                        .amount(balance)
                        .build();

                data.addFirst(fluctuation30DaysResponse);


            }

            balance = getRemaining(balance, fluctuation);
        }

        return data;
    }

    int getRemaining(int balance, Fluctuation fluctuation) {
        if (fluctuation.getTransaction().equals(ETransaction.CREDIT.name())) {
            balance -= fluctuation.getAmount();
        } else {
            balance += fluctuation.getAmount();
        }
        return balance;
    }


    List<String> getAllTypeReason() {
        List<String> allType = new ArrayList<>();
        for (EReason reason : EReason.values())
            allType.add(reason.getValue());

        return allType;
    }

}
