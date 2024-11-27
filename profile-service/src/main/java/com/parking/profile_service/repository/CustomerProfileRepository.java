package com.parking.profile_service.repository;

import com.parking.profile_service.entity.ProfileCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerProfileRepository extends JpaRepository<ProfileCustomer, String> {
    int countByPhone(String phone);

    @Query(value = "SELECT * FROM Profile_customer p WHERE p.name LIKE %:name% LIMIT :start, :end", nativeQuery = true)
    List<ProfileCustomer> getByLikeName(@Param("name") String name, @Param("start") int start, @Param("end") int end);
}
