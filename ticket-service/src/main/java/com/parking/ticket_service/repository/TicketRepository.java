package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    Optional<Ticket> findByIdAndUid(String id, String uid);

    Page<Ticket> findByUid(String uid, Pageable pageable);

    List<Ticket> findAllByUidAndUsedAtBetween(String uid, long start, long end);

    int countByUid(String uid);

//    @Query(value = "SELECT t1_0.id, t1_0.buyAt, t1_0.category, t1_0.contentPlate, t1_0.expireAt, t1_0.price, t1_0.startAt, t1_0.turnTotal, t1_0.uid, t1_0.usedAt " +
//            "FROM ticket t1_0 " +
//            "LEFT JOIN category c1_0 ON c1_0.id = t1_0.category " +
//            "WHERE ((t1_0.startAt BETWEEN :startAt and :expireAt) OR (t1_0.expireAt BETWEEN :startAt and :expireAt)) " +
//            "AND c1_0.vehicle = :vehicle")
//    List<Ticket> findAllTicket(long startAt, long expireAt, String vehicle);

    List<Ticket> findAllByStartAtBetweenOrExpireAtBetweenAndCategory_VehicleOrderByStartAtAsc(long startAt1, long expireAt1, long startAt2, long expireAt2, String vehicle);
}
