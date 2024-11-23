package com.parking.ticket_service.mapper;

import com.parking.ticket_service.dto.response.ItemCartResponse;
import com.parking.ticket_service.entity.Category;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "quantity", expression = "java(quantity)")
    ItemCartResponse toItemCartResponse(Category category, @Context int quantity);
}
