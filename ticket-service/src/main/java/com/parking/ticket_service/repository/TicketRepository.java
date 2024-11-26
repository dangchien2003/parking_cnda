package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    Optional<Ticket> findByIdAndUid(String id, String uid);

    Page<Ticket> findByUid(String uid, Pageable pageable);

    Page<Ticket> findByUidAndCategory_Vehicle(String uid, String vehicle, Pageable pageable);

    List<Ticket> findAllByUidAndUsedAtBetween(String uid, long start, long end);

    int countByUid(String uid);

    @Query("SELECT t FROM ticket t " +
            "JOIN t.category c " +
            "WHERE (t.startAt BETWEEN :start1 AND :end1 OR t.expireAt BETWEEN :start2 AND :end2) " +
            "AND c.vehicle = :vehicle " +
            "ORDER BY t.startAt ASC")
    List<Ticket> findTickets(@Param("start1") Long start1,
                             @Param("end1") Long end1,
                             @Param("start2") Long start2,
                             @Param("end2") Long end2,
                             @Param("vehicle") String vehicle);
}
