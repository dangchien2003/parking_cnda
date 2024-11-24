package com.parking.vault_service.service;

import com.parking.vault_service.dto.request.DepositApproveRequest;
import com.parking.vault_service.dto.request.DepositCreationRequest;
import com.parking.vault_service.dto.request.StaffCancelDepositRequest;
import com.parking.vault_service.dto.response.DepositResponse;
import com.parking.vault_service.dto.response.HistoryDeposit;
import com.parking.vault_service.dto.response.PageResponse;
import com.parking.vault_service.dto.response.TransactionInfo;
import com.parking.vault_service.entity.Deposit;
import com.parking.vault_service.entity.Fluctuation;
import com.parking.vault_service.entity.Owner;
import com.parking.vault_service.enums.EGetAllDeposit;
import com.parking.vault_service.enums.EPageQuantity;
import com.parking.vault_service.exception.AppException;
import com.parking.vault_service.exception.ErrorCode;
import com.parking.vault_service.mapper.DepositMapper;
import com.parking.vault_service.repository.DepositRepository;
import com.parking.vault_service.repository.OwnerRepository;
import com.parking.vault_service.utils.ENumUtil;
import com.parking.vault_service.utils.PageUtil;
import com.parking.vault_service.utils.PaymentUtils;
import com.parking.vault_service.utils.TimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DepositService {
    VnPayService vnPayService;
    DepositRepository depositRepository;
    OwnerRepository ownerRepository;
    DepositMapper depositMapper;
    ApproveDeposit approveDeposit;

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public List<TransactionInfo> getHistoryBanked(String date) {
        return approveDeposit.getHistory(date);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public List<Fluctuation> approveDeposit(DepositApproveRequest request) {

        List<Deposit> deposits = depositRepository.findByIdInAndActionAtIsNull(Arrays.asList(request.getDeposits()));

        if (deposits.isEmpty())
            throw new AppException(ErrorCode.DEPOSIT_NOT_EXIST_OR_BEEN_APPROVED);

        return approveDeposit.approveAction(deposits, "Staff approves deposit request");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public DepositResponse create(HttpServletRequest http, DepositCreationRequest request) {

        String uid = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Owner owner = ownerRepository.findById(uid)
                .orElseThrow(() -> new AppException(ErrorCode.OWNER_NOT_EXIST));

        int numDepositWaiting = depositRepository.countByOwnerIdAndCancelAtIsNullAndActionAtIsNull(owner.getId());

        if (numDepositWaiting >= 3)
            throw new AppException(ErrorCode.MANY_DEPOSIT);

        String id = UUID.randomUUID().toString();
        while (true) {
            if (depositRepository.existsById(id))
                id = UUID.randomUUID().toString();
            else
                break;
        }

        Deposit deposit = depositMapper.toDeposit(request);
        deposit.setOwnerId(owner.getId());
        deposit.setId(id);
        deposit.setCreateAt(Instant.now().toEpochMilli());

        deposit = depositRepository.save(deposit);

        DepositResponse response = depositMapper.toDepositResponse(deposit);

        String ip = PaymentUtils.getClientIP(http);
        try {
            response.setLinkPayment(vnPayService.generateUrl(id, deposit.getAmount(), ip));
        } catch (UnsupportedEncodingException e) {
            throw new AppException(ErrorCode.UPDATE_FAIL);
        }
        return response;
    }

    public void checkDeposit(HttpServletRequest request, String depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_EXIST_OR_BEEN_APPROVED));

        if (deposit.getActionAt() != null)
            throw new AppException(ErrorCode.DEPOSIT_NOT_EXIST_OR_BEEN_APPROVED);

        if (!vnPayService.checkPaymentSuccess(request, deposit)) {
            log.warn("chưa thanh toán: " + depositId);
            return;
        }

        deposit.setActionAt(Instant.now().toEpochMilli());
        deposit.setActionBy("CALLBACK VNPAY");
        depositRepository.save(deposit);

        Owner owner = ownerRepository.findById(deposit.getOwnerId())
                .orElse(null);

        if (owner == null) {
            log.warn("owner not exist of deposit: " + depositId);
            return;
        }

        owner.setBalance(owner.getBalance() + deposit.getAmount());
        ownerRepository.save(owner);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public PageResponse<Deposit> getAll(String type, int page, String sort) {

        EGetAllDeposit eGetAllDeposit;

        try {
            eGetAllDeposit = ENumUtil.getType(EGetAllDeposit.class, type);
        } catch (AppException e) {
            throw new AppException(ErrorCode.NOTFOUND_URL);
        }

        Pageable pageable = PageUtil.getPageable(page, EPageQuantity.DEPOSIT.getQuantity(), sort, "createAt");

        Page<Deposit> pageData;

        switch (eGetAllDeposit) {
            case ANY -> pageData = depositRepository.findAll(pageable);
            case WAITING -> pageData = depositRepository.findAllByActionAtIsNullAndCancelAtIsNull(pageable);
            case APPROVED -> pageData = depositRepository.findAllByActionAtIsNotNull(pageable);
            case CANCELED -> pageData = depositRepository.findAllByCancelAtIsNotNull(pageable);
            default -> throw new AppException(ErrorCode.UNSUPPORTED_FILTERS);
        }

        return PageUtil.renderPageResponse(pageData.getContent(), page, pageData.getSize());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public void cancelDeposit(String id) {
        String uid = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Owner owner = ownerRepository.findById(uid)
                .orElseThrow(() -> new AppException(ErrorCode.OWNER_NOT_EXIST));

        Deposit deposit = depositRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_FAIL));

        if (!Objects.isNull(deposit.getCancelAt()) || !Objects.isNull(deposit.getActionAt()))
            throw new AppException(ErrorCode.UPDATE_FAIL);

        deposit.setCancelAt(Instant.now().toEpochMilli());
        depositRepository.save(deposit);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STAFF')")
    public List<String> cancelDeposit(StaffCancelDepositRequest request) {
        List<Deposit> deposits = depositRepository
                .findByIdInAndActionAtIsNullAndCancelAtIsNull(
                        request.getDepositsId());

        if (deposits.isEmpty())
            throw new AppException(ErrorCode.UPDATE_FAIL);

        List<String> depositsCancel = new ArrayList<>();
        long now = Instant.now().toEpochMilli();
        deposits.forEach(deposit -> {
            deposit.setCancelAt(now);
            depositsCancel.add(deposit.getId());
        });

        depositRepository.saveAll(deposits);
        return depositsCancel;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public PageResponse<DepositResponse> customerGetAll(String type, int page, String sort) {

        EGetAllDeposit eGetAllDeposit;

        try {
            eGetAllDeposit = ENumUtil.getType(EGetAllDeposit.class, type);
        } catch (AppException e) {
            throw new AppException(ErrorCode.NOTFOUND_URL);
        }

        String owner = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Pageable pageable = PageUtil.getPageable(page, EPageQuantity.DEPOSIT.getQuantity(), sort, "CreateAt");

        Page<Deposit> pageData;

        switch (eGetAllDeposit) {
            case ANY -> pageData = depositRepository.findAll(pageable);
            case WAITING -> pageData = depositRepository.findAllByOwnerIdAndActionAtIsNull(owner, pageable);
            case APPROVED -> pageData = depositRepository.findAllByOwnerIdAndActionAtIsNotNull(owner, pageable);
            case CANCELED -> pageData = depositRepository.findAllByOwnerIdAndCancelAtIsNotNull(owner, pageable);
            default -> throw new AppException(ErrorCode.UNSUPPORTED_FILTERS);
        }

        List<DepositResponse> depositResponses = pageData.getContent().stream()
                .map(depositMapper::toDepositResponse)
                .toList();

        return PageUtil.renderPageResponse(depositResponses, page, pageData.getSize());
    }


    public Integer totalDeposit() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();
//        String uid = "7c11b1ab-0c8a-40c3-93ea-65b8202fce29";

        int total = depositRepository.calculateTotalAmountWhereOwnerId(uid);
        return total;
    }

    public Integer totalApproved() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();
//        String uid = "7c11b1ab-0c8a-40c3-93ea-65b8202fce29";

        int total = totalDeposit();
        int wait = totalWaitApprove();


        return total - wait;
    }

    public Integer totalWaitApprove() {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();
//        String uid = "7c11b1ab-0c8a-40c3-93ea-65b8202fce29";
        int total = depositRepository.calculateTotalWaitApproveWhereOwnerId(uid);
        return total;
    }

    public List<HistoryDeposit> history(int page, String status, String date) {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();
//        String uid = "7c11b1ab-0c8a-40c3-93ea-65b8202fce29";
        Pageable pageable = PageUtil.getPageable(page, 10, PageUtil.getSort("DESC", "createAt"));
        List<Deposit> deposits;
        if (status == null && date == null) {
            deposits = depositRepository.findAllByOwnerId(uid, pageable);
        } else if (status != null && date == null) {
            if (status.equals("approved"))
                deposits = depositRepository.findAllByOwnerIdAndActionAtIsNotNull(uid, pageable).getContent();
            else
                deposits = depositRepository.findAllByOwnerIdAndActionAtIsNull(uid, pageable).getContent();
        } else if (status == null && date != null) {
            long start = TimeUtils.getStartOfDay(date);
            long end = TimeUtils.getEndOfDay(date);
            deposits = depositRepository.findAllByCreateAtIsBetweenAndOwnerId(start, end, uid, pageable);
        } else {
            long start = TimeUtils.getStartOfDay(date);
            long end = TimeUtils.getEndOfDay(date);
            if (status.equals("approved"))
                deposits = depositRepository.findAllByCreateAtIsBetweenAndOwnerIdAndActionAtIsNotNull(start, end, uid, pageable);
            else
                deposits = depositRepository.findAllByCreateAtIsBetweenAndOwnerIdAndActionAtIsNull(start, end, uid, pageable);
        }


        return deposits.stream().map(deposit -> {
            HistoryDeposit historyDeposit = new HistoryDeposit();
            historyDeposit.setAmount(deposit.getAmount());
            historyDeposit.setTime(TimeUtils.convertTime(deposit.getCreateAt(), "dd/MM/yyyy HH:mm"));
            historyDeposit.setStatus(deposit.getActionAt() == null ? "Chờ duyệt" : "Đã duyệt");
            return historyDeposit;
        }).toList();
    }
}
