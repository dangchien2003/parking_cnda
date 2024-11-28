package com.parking.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class thong_tin_tai_khoan {
    String id;
    String email;
    String name;
    String phone;
    int tieu_dung_trong_thang;
    int so_ve_da_mua;
    int so_du;
    String status;
}
