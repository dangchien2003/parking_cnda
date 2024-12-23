package com.parking.ticket_service.service;

import com.parking.ticket_service.dto.request.AddCartRequest;
import com.parking.ticket_service.dto.request.UpdateQuantityCartItemRequest;
import com.parking.ticket_service.dto.response.CountQuantityItemInCartResponse;
import com.parking.ticket_service.dto.response.ItemCartResponse;
import com.parking.ticket_service.entity.Cart;
import com.parking.ticket_service.entity.CartId;
import com.parking.ticket_service.entity.Category;
import com.parking.ticket_service.enums.ECategoryStatus;
import com.parking.ticket_service.exception.AppException;
import com.parking.ticket_service.exception.ErrorCode;
import com.parking.ticket_service.mapper.CartMapper;
import com.parking.ticket_service.repository.CartRepository;
import com.parking.ticket_service.repository.CategoryRepository;
import com.parking.ticket_service.utils.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CartService {
    CartRepository cartRepository;
    CategoryRepository categoryRepository;
    CartMapper cartMapper;


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
    public List<ItemCartResponse> getAll(int page) {
        int pageSize = 20;
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy danh sách carts
        List<Cart> carts = cartRepository.findAllById_Uid(user,
                PageUtils.getPageable(1, pageSize, PageUtils.getSort("DESC", "createdAt")));
        carts.sort((c1, c2) -> {
            if (c1.getId() == null || c1.getId().getTicketId() == null) return -1;
            if (c2.getId() == null || c2.getId().getTicketId() == null) return 1;
            return c1.getId().getTicketId().compareTo(c2.getId().getTicketId());
        });

        // Lấy danh sách categoryIds từ carts
        List<String> categoryIds = carts.stream()
                .map(cart -> cart.getId().getTicketId())
                .toList();

        // Lấy danh sách categories từ categoryIds
        List<Category> categories = categoryRepository.findAllByIdIn(categoryIds,
                PageUtils.getPageable(page, pageSize, PageUtils.getSort("DESC", "id")));

        // Tạo Map ánh xạ từ categoryId sang Category
        Map<String, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        // Duyệt qua danh sách carts và ánh xạ với categories theo thứ tự
        List<ItemCartResponse> responses = new ArrayList<>();
        for (Cart cart : carts) {
            String ticketId = cart.getId().getTicketId();
            Category category = categoryMap.get(ticketId);

            if (category == null) {
                log.error("Không tìm thấy category cho ticketId: {}", ticketId);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            // Thêm vào danh sách responses
            responses.add(cartMapper.toItemCartResponse(category, cart.getQuantity()));
        }

        return responses;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public UpdateQuantityCartItemRequest updateQuantity(UpdateQuantityCartItemRequest request) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        CartId cartId = new CartId(request.getTicketId(), user);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        cart.setQuantity(request.getQuantity());

        cartRepository.save(cart);
        return request;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public void moveItem(String ticketId) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        CartId cartId = new CartId(ticketId, user);
        if (cartRepository.existsById(cartId))
            cartRepository.deleteById(cartId);
        else
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
    }
}
