package com.parking.vault_service.mapper;

import com.parking.vault_service.dto.response.AddFluctuationResponse;
import com.parking.vault_service.entity.Deposit;
import com.parking.vault_service.entity.Fluctuation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FluctuationMapper {
    @Mapping(source = "id", target = "depositId")
    @Mapping(target = "createAt", ignore = true)
    Fluctuation toFluctuation(Deposit deposit);

    AddFluctuationResponse toAddFuctuationResponse(Fluctuation fluctuation);
}
