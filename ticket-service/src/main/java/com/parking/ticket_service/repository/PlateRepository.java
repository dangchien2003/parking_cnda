package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Plate;
import com.parking.ticket_service.entity.PlateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlateRepository extends JpaRepository<Plate, PlateId> {
    @Query(value = "SELECT COUNT(*) FROM plate p WHERE p.ticket_id IN :ticketIds AND used_at BETWEEN :start AND :end", nativeQuery = true)
    int countByTicketIds(@Param("ticketIds") List<String> ticketIds, @Param("start") long start, @Param("end") long end);
}
