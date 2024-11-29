package com.parking.ticket_service.mapper;

import com.parking.ticket_service.dto.request.CategoryCreatitonRequest;
import com.parking.ticket_service.dto.response.CategoryResponse;
import com.parking.ticket_service.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreatitonRequest request);


    CategoryResponse toCategoryResponse(Category category);
}
