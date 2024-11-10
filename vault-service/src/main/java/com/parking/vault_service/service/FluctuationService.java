package com.parking.vault_service.service;

import com.parking.vault_service.dto.request.AddFluctuationRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        addFluctuation(request.getAmount(), eTransaction, EReason.CANCEL_TICKET, description);
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
    public List<FluctuationResponse> getAllByCustomer(String type, int page, String sort, String field) {
        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();
//        String uid = "7c11b1ab-0c8a-40c3-93ea-65b8202fce29";

        List<Fluctuation> fluctuations = getAll(type, uid, page, sort, field).getData();
        return fluctuations.stream().map(fluctuation -> {
            FluctuationResponse fluctuationResponse = new FluctuationResponse();
            fluctuationResponse.setTime(TimeUtils.convertTime(fluctuation.getCreateAt(), "dd/MM/yyyy HH:mm:ss"));
            fluctuationResponse.setAmount(fluctuation.getAmount());
            fluctuationResponse.setContain(fluctuation.getReason());
            return fluctuationResponse;
        }).toList();
    }

    PageResponse<Fluctuation> getAll(@NotNull String type, String uid, int page, String sort, String field) {
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
        for(Fluctuation fluctuation : fluctuations) {
            total += fluctuation.getAmount();
        }

        return total;
    }


    List<String> getAllTypeReason() {
        List<String> allType = new ArrayList<>();
        for (EReason reason : EReason.values())
            allType.add(reason.getValue());

        return allType;
    }

}
