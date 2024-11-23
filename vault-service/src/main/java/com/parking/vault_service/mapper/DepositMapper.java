package com.parking.vault_service.mapper;

import com.parking.vault_service.dto.request.DepositCreationRequest;
import com.parking.vault_service.dto.response.DepositResponse;
import com.parking.vault_service.entity.Deposit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepositMapper {

    Deposit toDeposit(DepositCreationRequest request);

    DepositResponse toDepositResponse(Deposit cash);
}
