package com.parking.ticket_service.mapper;

import com.parking.ticket_service.dto.response.QrResponse;
import com.parking.ticket_service.entity.QR;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QrMapper {
    QrResponse toQrResponse(QR qr);
}
