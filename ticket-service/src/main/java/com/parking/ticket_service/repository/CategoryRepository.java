package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Page<Category> findAllByStatusAndVehicle(String status, String vehicle, Pageable pageable);

    Page<Category> findAllByVehicle(String vehicle, Pageable pageable);

    List<Category> findAllByIdIn(List<String> ids, Pageable pageable);

}
