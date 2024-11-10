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

    int countByUid(String uid);
}
