package com.parking.ticket_service.mapper;

import com.parking.ticket_service.dto.request.UpdateSettingRequest;
import com.parking.ticket_service.entity.Setting;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SettingMapper {
    Setting toSetting(UpdateSettingRequest request);
}
