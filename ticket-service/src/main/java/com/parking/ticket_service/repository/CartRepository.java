package com.parking.ticket_service.repository;

import com.parking.ticket_service.entity.Cart;
import com.parking.ticket_service.entity.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    List<Cart> findAllById_Uid(String uid);

    int countById_Uid(String uid);
}
