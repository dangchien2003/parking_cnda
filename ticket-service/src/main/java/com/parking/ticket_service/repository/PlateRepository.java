package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Plate;
import com.parking.ticket_service.entity.PlateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlateRepository extends JpaRepository<Plate, PlateId> {
    @Query(value = "SELECT COUNT(*) FROM plate p WHERE p.ticket_id IN :ticketIds AND used_at BETWEEN :start AND :end order by turn DESC", nativeQuery = true)
    int countByTicketIds(@Param("ticketIds") List<String> ticketIds, @Param("start") long start, @Param("end") long end);

    List<Plate> findAllById_TicketId(String ticket);

    @Query(value = "SELECT plate from plate p where p.id.ticketId = :ticket ORDER BY p.id.turn DESC limit 1")
    Plate findNewByTicketId(@Param("ticket") String ticket);
}
