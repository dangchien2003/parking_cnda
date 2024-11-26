package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.response.PlateResponse;
import com.parking.ticket_service.entity.Plate;
import com.parking.ticket_service.mapper.PlateMapper;
import com.parking.ticket_service.repository.PlateRepository;
import com.parking.ticket_service.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlateService {
    PlateRepository plateRepository;
    PlateMapper plateMapper;

    public List<PlateResponse> getAllPlate(String ticketId) {
        List<Plate> plates = plateRepository.findAllById_TicketId(ticketId);

        if (plates.isEmpty()) {
            return new ArrayList<>();
        }

        return plates.stream().map(item -> {
            PlateResponse plateResponse = plateMapper.toPlateResponse(item);
            plateResponse.setCheckinTime(TimeUtils.convertTime(item.getCheckinAt(), "HH:mm dd/MM/yyyy"));
            plateResponse.setCheckoutTime(TimeUtils.convertTime(item.getCheckoutAt(), "HH:mm dd/MM/yyyy"));
            return plateResponse;
        }).toList();
    }
}
