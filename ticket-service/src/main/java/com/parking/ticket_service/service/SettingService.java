package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.request.UpdateSettingRequest;
import com.parking.ticket_service.entity.Setting;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.mapper.SettingMapper;
import com.parking.ticket_service.repository.SettingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SettingService {
    SettingRepository settingRepository;
    SettingMapper settingMapper;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public void update(UpdateSettingRequest request) {
        Setting setting = getNewRecord();
        Setting newSetting = settingMapper.toSetting(request);
        newSetting.setId(setting.getId());
        validateSetting(newSetting);
        settingRepository.save(newSetting);
    }

    void validateSetting(Setting setting) {
        if (setting.getSpareCar() > setting.getMaxPositionCar() * 0.6) {
            throw new AppException("Số vị trí dự trữ của ô tô không được vượt quá 60% tổng vị trí");
        }

        if (setting.getSpareMotorbike() > setting.getMaxPositionMotorbike() * 0.6) {
            throw new AppException("Số vị trí dự trữ của xe máy không được vượt quá 60% tổng vị trí");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Setting getNewRecord() {
        List<Setting> settings = settingRepository.findAllByOrderByIdDesc();
        if (settings.size() == 0) {
            return new Setting();
        }

        return settings.getFirst();
    }
}
