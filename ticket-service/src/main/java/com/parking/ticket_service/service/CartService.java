package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.request.AddCartRequest;
import com.parking.ticket_service.dto.response.CountQuantityItemInCartResponse;
import com.parking.ticket_service.entity.Cart;
import com.parking.ticket_service.entity.CartId;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.enums.ECategoryStatus;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.repository.CartRepository;
import com.parking.ticket_service.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CartService {
    CartRepository cartRepository;
    CategoryRepository categoryRepository;

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public void addCart(AddCartRequest request) {

        Category category = categoryRepository.findById(request.getTicketId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOTFOUND));

        if (category.getStatus().equalsIgnoreCase(ECategoryStatus.INACTIVE.name()))
            throw new AppException(ErrorCode.NOTFOUND_CATEGORY_UNIT);


        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        CartId id = new CartId(request.getTicketId(), user);

        Cart cart = cartRepository.findById(id).orElse(null);

        if (cart != null) {
            cart.setQuantity(cart.getQuantity() + 1);
        } else {
            cart = Cart.builder()
                    .id(id)
                    .quantity(1)
                    .build();
        }

        cartRepository.save(cart);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public CountQuantityItemInCartResponse count() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return new CountQuantityItemInCartResponse(cartRepository.countById_Uid(user));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public CountQuantityItemInCartResponse getAll(int page) {
        int pageSize = 20;
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Cart> carts = cartRepository.findAllById_Uid(user);

        return new CountQuantityItemInCartResponse(1);
    }
}
