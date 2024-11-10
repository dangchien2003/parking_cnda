package com.parking.ticket_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    int currentPage;

    int pageSize;

    @Builder.Default
    List<T> data = Collections.emptyList();
}
