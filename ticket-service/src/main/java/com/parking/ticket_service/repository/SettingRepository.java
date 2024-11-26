package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Integer> {
    List<Setting> findAllByOrderByIdDesc();
}
