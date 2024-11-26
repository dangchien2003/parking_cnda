package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.response.QrResponse;
import com.parking.ticket_service.entity.QR;
import com.parking.ticket_service.entity.Ticket;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.mapper.QrMapper;
import com.parking.ticket_service.repository.QRRepository;
import com.parking.ticket_service.repository.TicketRepository;
import com.parking.ticket_service.utils.AESUtils;
import com.parking.ticket_service.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QRService {
    QRRepository qrRepository;
    TicketRepository ticketRepository;
    QrMapper qrMapper;

    public List<QrResponse> getAllQrByTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));
        return qrRepository.findAllByTicketIdOrderByCreateAtDesc(ticket.getId()).stream()
                .map(item -> {
                    QrResponse qrResponse = qrMapper.toQrResponse(item);
                    qrResponse.setCreateTime(TimeUtils.convertTime(item.getCreateAt(), "HH:mm:ss dd/MM/yyyy"));
                    return qrResponse;
                }).toList();
    }

    public QrResponse getNew(String ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));

        validTicket(ticket);

        List<QR> qrs = qrRepository.findAllByTicketIdOrderByCreateAtDesc(ticket.getId());

        if (qrs.isEmpty())
            throw new AppException("Cần tạo mã QR trước");
        QrResponse qrResponse = qrMapper.toQrResponse(qrs.getFirst());
        qrResponse.setCreateTime(TimeUtils.convertTime(qrs.getFirst().getCreateAt(), "HH:mm:ss dd/MM/yyyy"));
        return qrResponse;
    }

    void validTicket(Ticket ticket) {
        String uid = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!ticket.getUid().equals(uid)) {
            throw new AppException(ErrorCode.TICKET_NOTFOUND);
        }

        long now = Instant.now().toEpochMilli();
        if (ticket.getStartAt() < now) {
            throw new AppException("Vé chưa thể sử dụng");
        } else if (ticket.getExpireAt() < now) {
            throw new AppException("Vé đã hết hạn");
        }
    }

    public QrResponse add(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));
        validTicket(ticket);
        try {
            QR qr = QR.builder()
                    .contain(AESUtils.encrypt(ticketId.getBytes()))
                    .createAt(Instant.now().toEpochMilli())
                    .ticketId(ticket.getId())
                    .build();

            qr = qrRepository.save(qr);

            QrResponse qrResponse = qrMapper.toQrResponse(qr);
            qrResponse.setCreateTime(TimeUtils.convertTime(qr.getCreateAt(), "HH:mm:ss dd/MM/yyyy"));
            return qrResponse;
        } catch (Exception e) {
            throw new AppException("Không thể tạo mã");
        }
    }
}
