package com.parking.ticket_service.mapper;

import com.parking.ticket_service.dto.response.PlateResponse;
import com.parking.ticket_service.entity.Plate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlateMapper {
    @Mapping(target = "turn", source = "id.turn")
    PlateResponse toPlateResponse(Plate plate);
}
