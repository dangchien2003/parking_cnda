package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.QR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QRRepository extends JpaRepository<QR, Integer> {
    List<QR> findAllByTicketIdOrderByCreateAtDesc(String ticketId);
}
