package com.parking.profile_service.repository;

import com.parking.profile_service.entity.ProfileCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<ProfileCustomer, String> {
    int countByPhone(String phone);
}
