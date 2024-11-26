package com.parking.ticket_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrResponse {
    @Id
    String contain;

    @NotNull
    String ticketId;

    long createAt;
    String createTime;
}
